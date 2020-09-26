package com.anod.car.home.prefs

import android.app.Activity
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.model.*
import com.anod.car.home.utils.DrawableUri
import com.anod.car.home.utils.ShortcutPicker
import com.anod.car.home.utils.UtilitiesBitmap
import com.anod.car.home.utils.forIconPack
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.startActivityForResultSafely
import kotlinx.android.synthetic.main.activity_shortcutedit.*

class ShortcutEditActivity : CarWidgetActivity() {

    private var customIcon: Bitmap? = null

    private var db: ShortcutsDatabase? = null

    private var shortcut: Shortcut? = null
    private var shortcutIcon: ShortcutIcon? = null
    private var iconDefault: Bitmap? = null

    private var containerModel: AbstractShortcuts? = null

    private var cellId: Int = 0

    override val appThemeRes: Int
        get() = theme.dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortcutedit)
        setTitle(R.string.shortcut_edit_title)

        init(intent)
    }

    private fun init(intent: Intent) {
        cellId = intent
                .getIntExtra(EXTRA_CELL_ID, ShortcutPicker.INVALID_CELL_ID)
        val shortcutId = intent
                .getLongExtra(EXTRA_SHORTCUT_ID, Shortcut.idUnknown)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
        if (cellId == ShortcutPicker.INVALID_CELL_ID || shortcutId == Shortcut.idUnknown) {
            AppLog.e("Missing parameter")
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        containerModel = if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            NotificationShortcutsModel.init(this)
        } else {
            WidgetShortcutsModel.init(this, appWidgetId)
        }
        db = containerModel!!.shortcutsDatabase

        shortcut = db!!.loadShortcut(shortcutId)
        shortcutIcon = containerModel!!.iconLoader.load(shortcut!!)

        labelEdit.setText(shortcut!!.title)
        iconView.setImageBitmap(shortcutIcon!!.bitmap)

        deleteButton.setOnClickListener {
            containerModel!!.drop(cellId)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        iconView.setOnClickListener {
            createIconMenu().show()
        }

        okButton.setOnClickListener {
            var needUpdate = false
            if (customIcon != null) {
                shortcutIcon = ShortcutIcon.forCustomIcon(shortcutIcon!!.id, customIcon!!)
                needUpdate = true
            } else if (iconDefault != null) {
                shortcutIcon = ShortcutIcon.forActivity(shortcutIcon!!.id, iconDefault!!)
                needUpdate = true
            }
            val title = labelEdit.text
            if (title != shortcut!!.title) {
                shortcut = Shortcut(shortcut!!.id, shortcut!!.itemType, title, shortcutIcon!!.isCustom, shortcut!!.intent)
                needUpdate = true
            }
            if (needUpdate) {
                db!!.updateItemInDatabase(this, shortcut!!, shortcutIcon!!)
            }

            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        init(intent)
    }

    private fun createIconMenu(): Dialog {
        val items: Array<CharSequence> = if (shortcut!!.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
            arrayOf(
                getString(R.string.icon_custom), // PICK_CUSTOM_ICON
                getString(R.string.icon_adw_icon_pack), // PICK_ADW_ICON_PACK
                getString(R.string.icon_default) // PICK_DEFAULT_ICON
            )
        } else {
            arrayOf(
                getString(R.string.icon_custom), // PICK_CUSTOM_ICON
                getString(R.string.icon_adw_icon_pack) // PICK_ADW_ICON_PACK
            )
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_title_select))
        builder.setItems(items) { _, item -> iconDialogClick(item) }
        return builder.create()
    }

    private fun iconDialogClick(item: Int) {
        val chooseIntent: Intent
        if (item == PICK_CUSTOM_ICON) {
            val tempFile = getFileStreamPath("tempImage")
            chooseIntent = Intent(Intent.ACTION_GET_CONTENT)
            chooseIntent.type = MIME_IMAGE
            chooseIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile))
            chooseIntent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.name)
            startActivityForResultSafely(chooseIntent, PICK_CUSTOM_ICON)
        } else if (item == PICK_ADW_ICON_PACK) {
            chooseIntent = Intent().forIconPack()
            startActivityForResultSafely(
                    Intent.createChooser(chooseIntent, getString(R.string.select_icon_pack)),
                    PICK_ADW_ICON_PACK)
        } else if (item == PICK_DEFAULT_ICON) {

            val componentName = shortcut!!.intent.component
            if (componentName == null) {
                Toast.makeText(this, R.string.failed_fetch_icon, Toast.LENGTH_LONG).show()
                return
            }
            val manager = packageManager
            val resolveInfo = manager.resolveActivity(shortcut!!.intent, 0)
            val icon = ShortcutInfoUtils.getIcon(componentName, resolveInfo, manager, this)
            if (icon != null) {
                iconDefault = icon
                customIcon = null
                iconView.setImageBitmap(icon)
            } else {
                Toast.makeText(this, R.string.failed_fetch_icon, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateCustomIcon(data: Intent) {
        val imageUri = data.data ?: return
        val icon = DrawableUri(this).resolve(imageUri)
        if (icon == null) {
            val errStr = getString(R.string.error_text,
                    getString(R.string.custom_image_error))
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show()
            return
        }
        val bitmap = UtilitiesBitmap.createMaxSizeIcon(icon, this)
        setCustomIcon(bitmap)
    }

    private fun setCustomIcon(icon: Bitmap) {
        customIcon = icon
        iconDefault = null
        iconView.setImageBitmap(customIcon)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_CUSTOM_ICON) {
                updateCustomIcon(data)
            } else if (requestCode == PICK_ADW_ICON_PACK) {
                val bitmap = getBitmapIconPackIntent(data)
                if (bitmap != null) {
                    setCustomIcon(bitmap)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getBitmapIconPackIntent(data: Intent): Bitmap? {
        var bitmap: Bitmap? = null
        if (data.hasExtra("icon")) {
            bitmap = data.getParcelableExtra("icon")
        } else {
            val imageUri = data.data ?: return bitmap
            val icon = DrawableUri(this).resolve(imageUri)
            if (icon != null) {
                bitmap = UtilitiesBitmap.createHiResIconBitmap(icon, this)
            }
        }
        return bitmap
    }

    companion object {

        fun createIntent(context: Context, cellId: Int, shortcutId: Long,
                         appWidgetId: Int): Intent {
            val editIntent = Intent(context, ShortcutEditActivity::class.java)
            editIntent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
            editIntent.putExtra(EXTRA_CELL_ID, cellId)
            editIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            editIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return editIntent
        }

        private const val MIME_IMAGE = "image/*"
        const val EXTRA_SHORTCUT_ID = "extra_id"
        const val EXTRA_CELL_ID = "extra_cell_id"

        private const val PICK_CUSTOM_ICON = 0
        private const val PICK_ADW_ICON_PACK = 1
        private const val PICK_DEFAULT_ICON = 2
    }

}
