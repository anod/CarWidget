package com.anod.car.home.prefs.lookandfeel

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.android.colorpicker.ColorPickerSwatch
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.prefs.LookAndFeelActivity
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog
import com.anod.car.home.utils.FastBitmapDrawable
import com.anod.car.home.utils.HtmlCompat
import info.anodsplace.carwidget.content.model.WidgetShortcutsModel
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.preferences.WidgetStorage
import info.anodsplace.carwidget.preferences.DefaultsResourceProvider
import info.anodsplace.framework.app.DialogCustom
import info.anodsplace.framework.app.DialogMessage
import info.anodsplace.framework.app.DialogSingleChoice
import info.anodsplace.framework.content.startActivityForResultSafely

/**
 * @author alex
 * @date 2014-10-20
 */
class LookAndFeelMenu(private val activity: LookAndFeelActivity, private val model: WidgetShortcutsModel) {

//    private val appWidgetId: Int = activity.appWidgetId
    private var menuTileColor: MenuItem? = null
    private var initialized: Boolean = false
    private var menuInfo: MenuItem? = null

    fun onCreateOptionsMenu(toolbar: Toolbar, isIconsMono: Boolean) {
        toolbar.inflateMenu(R.menu.look_n_feel)
        val menu = toolbar.menu

        menuTileColor = menu.findItem(R.id.tile_color)
        menuInfo = menu.findItem(R.id.skin_info)
        menu.findItem(R.id.icons_mono).isChecked = isIconsMono
        initialized = true
        refresh()
    }

    fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
//        val prefs = WidgetStorage.load(activity, DefaultsResourceProvider(activity), appWidgetId)
//        return when (menuItem.itemId) {
//            R.id.apply -> {
//                prefs.skin = activity.currentSkinItem.value
//                prefs.apply()
//                activity.beforeFinish()
//                activity.finish()
//                return true
//            }
//            R.id.menu_number -> {
//                createNumberPickerDialog().show()
//                return true
//            }
//            R.id.choose_tile_color -> {
//                val value = prefs.tileColor!!
//                val d = CarHomeColorPickerDialog.newInstance(value, true, activity)
//                d.listener = ColorPickerSwatch.OnColorSelectedListener { color ->
//                    prefs.tileColor = color
//                    prefs.paletteBackground = false
//                    prefs.apply()
//                    showTileColorButton()
//                    activity.refreshSkinPreview()
//                }
//                d.show(activity.supportFragmentManager, "tileColor")
//                return true
//            }
//            R.id.palette_background -> {
//                prefs.paletteBackground = !prefs.paletteBackground
//                prefs.apply()
//                showTileColorButton()
//                activity.refreshSkinPreview()
//                return true
//            }
//            R.id.more -> {
////                val intent = ConfigurationActivity
////                        .createFragmentIntent(activity, ConfigurationLook::class.java)
////                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
////                activity.startActivityForResult(intent, REQUEST_LOOK_ACTIVITY)
//                return true
//            }
//            R.id.bg_color -> {
//                val value = prefs.backgroundColor
//                val d = CarHomeColorPickerDialog.newInstance(value, true, activity)
//                d.listener = ColorPickerSwatch.OnColorSelectedListener { color ->
//                    prefs.backgroundColor = color
//                    prefs.apply()
//                    activity.refreshSkinPreview()
//                }
//                d.show(activity.supportFragmentManager, "bgColor")
//                return true
//            }
//            R.id.icons_theme -> {
//                val mainIntent = Intent(activity, IconThemesActivity::class.java)
//                mainIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
//                activity.startActivityForResultSafely(mainIntent, REQUEST_PICK_ICON_THEME)
//                return true
//            }
//            R.id.icons_mono -> {
//                prefs.isIconsMono = !menuItem.isChecked
//                activity.persistPrefs()
//                menuItem.isChecked = !menuItem.isChecked
//                activity.refreshSkinPreview()
//                return true
//            }
//            R.id.icons_scale -> {
//                val values = activity.resources.getStringArray(R.array.icon_scale_values)
//                val scale = prefs.iconsScale
//                val idx = values.indexOf(scale)
//                val style = R.style.Alert
//                DialogSingleChoice(activity, style, R.string.pref_scale_icon, R.array.icon_scale_titles, idx) {
//                    dialog, which ->
//                    prefs.setIconsScaleString(values[which])
//                    activity.persistPrefs()
//                    dialog.dismiss()
//                    activity.refreshSkinPreview()
//                }.show()
//                return true
//            }
//            R.id.skin_info -> {
//                val style = R.style.Alert
//                DialogMessage(
//                        context = activity,
//                        themeResId = style,
//                        titleRes = R.string.info,
//                        message = HtmlCompat.fromHtml(activity.getString(R.string.skin_info_bbb))) {
//                    it.setCancelable(true)
//                    it.setPositiveButton(android.R.string.ok) { _, _ -> }
//                }.show()
//                return true
//            }
//            else -> false
//        }
        return false
    }

    fun refresh() {
//        if (initialized) {
//            showTileColorButton()
//            menuInfo!!.isVisible = activity.currentSkinItem.value == WidgetInterface.SKIN_BBB
//        }
    }

    private fun showTileColorButton() {
//        menuTileColor?.also {
//            if (activity.currentSkinItem.value == WidgetInterface.SKIN_WINDOWS7) {
//                val prefs = WidgetStorage.load(activity, DefaultsResourceProvider(activity), appWidgetId)
//                it.subMenu.findItem(R.id.palette_background).isChecked = prefs.paletteBackground
//                if (prefs.paletteBackground || Color.alpha(prefs.tileColor!!) == 0) {
//                    it.icon = activity.getDrawable(R.drawable.ic_format_color_fill_black_24dp)
//                } else {
//                    val size = activity.resources.getDimension(R.dimen.color_preview_size).toInt()
//
//                    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
//                    val c = Canvas(bitmap)
//                    c.drawColor(prefs.tileColor!!)
//                    val d = FastBitmapDrawable(bitmap)
//
//                    it.icon = d
//                }
//                it.isVisible = true
//            } else {
//                it.isVisible = false
//            }
//        }
    }


    private fun createNumberPickerDialog(): AlertDialog {
        val nums = activity.resources.getStringArray(R.array.shortcut_numbers)

        var index = 0
        val countStr = model.count.toString()
        for (i in nums.indices) {
            if (countStr == nums[i]) {
                index = i
                break
            }
        }

        return DialogCustom(activity, R.style.Alert, R.string.number_shortcuts_title, R.layout.dialog_numberpicker) { view, builder ->

            val numberPicker = view.findViewById<View>(R.id.numberPicker) as NumberPicker
            numberPicker.minValue = 0
            numberPicker.maxValue = nums.size - 1
            numberPicker.displayedValues = nums
            numberPicker.value = index

            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                val value = numberPicker.value
                model.updateCount(Integer.valueOf(nums[value]))
//                activity.refreshSkinPreview()
            }

            builder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
        }.create()

    }

    companion object {
        const val REQUEST_LOOK_ACTIVITY = 1
        const val REQUEST_PICK_ICON_THEME = 2
    }
}
