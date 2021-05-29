package com.anod.car.home.prefs

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.databinding.FragmentShortcutEditBinding
import com.anod.car.home.model.*
import com.anod.car.home.utils.*
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.ShortcutIcon
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.intent.IntentEditFragment
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.model.AbstractShortcuts
import info.anodsplace.carwidget.content.model.NotificationShortcutsModel
import info.anodsplace.carwidget.content.model.ShortcutInfoUtils
import info.anodsplace.carwidget.content.model.WidgetShortcutsModel
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.framework.app.DialogItems
import info.anodsplace.framework.app.FragmentContainerActivity
import info.anodsplace.framework.content.forIconPack

class ShortcutEditFragment : Fragment() {

    private lateinit var pickAdwIconPack: ActivityResultLauncher<Intent>
    private lateinit var pickCustomIcon: ActivityResultLauncher<ActivityResultRequest.PickImage.Args>

    private var _binding: FragmentShortcutEditBinding? = null
    private val binding get() = _binding!!

    private val db: ShortcutsDatabase
        get() = containerModel!!.shortcutsDatabase
    private val iconLoader: ShortcutIconLoader
        get() = containerModel!!.iconLoader

    private var customIcon: Bitmap? = null
    private var shortcut: Shortcut? = null
    private var shortcutIcon: ShortcutIcon? = null
    private var iconDefault: Bitmap? = null
    private var containerModel: AbstractShortcuts? = null
    private var cellId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pickCustomIcon = registerForActivityResult(ActivityResultRequest.PickImage()) { imageUri ->
            if (imageUri != null) {
                updateCustomIcon(imageUri)
            }
        }

        pickAdwIconPack = registerForActivityResult(ActivityResultRequest.CreateChooser(R.string.select_icon_pack)) { data ->
            if (data != null) {
                val bitmap = getBitmapIconPackIntent(data)
                if (bitmap != null) {
                    setCustomIcon(bitmap)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentShortcutEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(requireArguments())
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.intent_advanced -> {
                    val intent = FragmentContainerActivity.intent(
                        requireContext(),
                        IntentEditFragment.Factory(shortcut!!.intent.toUri(0))
                    )
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun init(args: Bundle) {
        cellId = args.getInt(extraCellId, ShortcutPicker.INVALID_CELL_ID)
        val shortcutId = args.getLong(extraShortcutId, Shortcut.idUnknown)
        val appWidgetId = args.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (cellId == ShortcutPicker.INVALID_CELL_ID || shortcutId == Shortcut.idUnknown) {
            AppLog.e("Missing parameter")
            parentFragmentManager.popBackStack()
            return
        }

        containerModel = if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            NotificationShortcutsModel.init(requireContext())
        } else {
            WidgetShortcutsModel.init(requireContext(), DefaultsResourceProvider(requireContext()), appWidgetId)
        }

        shortcut = db.loadShortcut(shortcutId)
        shortcutIcon = iconLoader.load(shortcut!!)
        binding.labelEdit.setText(shortcut!!.title)
        binding.iconView.setImageBitmap(shortcutIcon!!.bitmap)

        binding.deleteButton.setOnClickListener {
            containerModel!!.drop(cellId)
            parentFragmentManager.popBackStack()
        }

        binding.iconView.setOnClickListener {
            createIconMenu().show()
        }

        binding.okButton.setOnClickListener {
            var needUpdate = false
            if (customIcon != null) {
                shortcutIcon = ShortcutIcon.forCustomIcon(shortcutIcon!!.id, customIcon!!)
                needUpdate = true
            } else if (iconDefault != null) {
                shortcutIcon = ShortcutIcon.forActivity(shortcutIcon!!.id, iconDefault!!)
                needUpdate = true
            }
            val title = binding.labelEdit.text
            if (title != shortcut!!.title) {
                shortcut = Shortcut(shortcut!!.id, shortcut!!.itemType, title, shortcutIcon!!.isCustom, shortcut!!.intent)
                needUpdate = true
            }
            if (needUpdate) {
                db.updateItemInDatabase(requireContext(), shortcut!!, shortcutIcon!!)
            }

            parentFragmentManager.popBackStack()
        }

    }

    private fun createIconMenu(): DialogItems {
        return DialogItems(
                context = requireContext(),
                themeResId = App.theme(requireContext()).alert,
                titleRes = R.string.dialog_choose_icon,
                itemsRes = if (shortcut!!.isApp) R.array.edit_icon_app else R.array.edit_icon_custom) { _, item -> iconDialogClick(item) }
    }

    private fun iconDialogClick(item: Int) {
        if (item == PICK_CUSTOM_ICON) {
            val tempFile = requireContext().getFileStreamPath("tempImage")
            pickCustomIcon.launch(ActivityResultRequest.PickImage.Args(output = Uri.fromFile(tempFile)))
        } else if (item == PICK_ADW_ICON_PACK) {
            pickAdwIconPack.launch(Intent().forIconPack())
        } else if (item == PICK_DEFAULT_ICON) {
            val componentName = shortcut!!.intent.component
            if (componentName == null) {
                Toast.makeText(requireContext(), R.string.failed_fetch_icon, Toast.LENGTH_LONG).show()
                return
            }
            val manager = requireContext().packageManager
            val resolveInfo = manager.resolveActivity(shortcut!!.intent, 0)
            val icon = ShortcutInfoUtils.getIcon(componentName, resolveInfo, manager, requireContext())
            if (icon != null) {
                iconDefault = icon
                customIcon = null
                binding.iconView.setImageBitmap(icon)
            } else {
                Toast.makeText(requireContext(), R.string.failed_fetch_icon, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateCustomIcon(imageUri: Uri) {
        val icon = DrawableUri(requireContext()).resolve(imageUri)
        if (icon == null) {
            val errStr = getString(R.string.error_text,
                    getString(R.string.custom_image_error))
            Toast.makeText(requireContext(), errStr, Toast.LENGTH_LONG).show()
            return
        }
        val bitmap = UtilitiesBitmap.createMaxSizeIcon(icon, requireContext())
        setCustomIcon(bitmap)
    }

    private fun setCustomIcon(icon: Bitmap) {
        customIcon = icon
        iconDefault = null
        binding.iconView.setImageBitmap(customIcon)
    }

    private fun getBitmapIconPackIntent(data: Intent): Bitmap? {
        var bitmap: Bitmap? = null
        if (data.hasExtra("icon")) {
            bitmap = data.getParcelableExtra("icon")
        } else {
            val imageUri = data.data ?: return bitmap
            val icon = DrawableUri(requireContext()).resolve(imageUri)
            if (icon != null) {
                bitmap = UtilitiesBitmap.createHiResIconBitmap(icon, requireContext())
            }
        }
        return bitmap
    }

    companion object {
        fun create(extras: Bundle) = ShortcutEditFragment().apply {
                arguments = bundleOf(
                        extraCellId to extras.getInt(extraCellId, ShortcutPicker.INVALID_CELL_ID),
                        extraShortcutId to extras.getLong(extraShortcutId, Shortcut.idUnknown),
                        AppWidgetManager.EXTRA_APPWIDGET_ID to extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                )
            }

        const val extraShortcutId = "extra_id"
        const val extraCellId = "extra_cell_id"

        private const val PICK_CUSTOM_ICON = 0
        private const val PICK_ADW_ICON_PACK = 1
        private const val PICK_DEFAULT_ICON = 2
    }
}