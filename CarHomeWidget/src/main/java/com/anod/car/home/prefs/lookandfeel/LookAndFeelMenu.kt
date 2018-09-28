package com.anod.car.home.prefs.lookandfeel

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import com.android.colorpicker.ColorPickerSwatch
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.backup.ui.FragmentBackup
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.prefs.ConfigurationActivity
import com.anod.car.home.prefs.ConfigurationLook
import com.anod.car.home.prefs.LookAndFeelActivity
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog
import com.anod.car.home.prefs.model.WidgetInterface
import com.anod.car.home.prefs.model.WidgetStorage
import com.anod.car.home.utils.FastBitmapDrawable
import com.anod.car.home.utils.HtmlCompat
import com.anod.car.home.utils.Utils
import info.anodsplace.framework.app.DialogMessage
import info.anodsplace.framework.app.DialogSingleChoice

/**
 * @author alex
 * @date 2014-10-20
 */
class LookAndFeelMenu(private val activity: LookAndFeelActivity, private val model: WidgetShortcutsModel) {

    private val appWidgetId: Int = activity.appWidgetId
    private var menuTileColor: MenuItem? = null
    private var initialized: Boolean = false
    private var menuInfo: MenuItem? = null

    fun onCreateOptionsMenu(menu: Menu) {
        val menuInflater = activity.menuInflater
        menuInflater.inflate(R.menu.look_n_feel, menu)

        menuTileColor = menu.findItem(R.id.tile_color)
        menuInfo = menu.findItem(R.id.skin_info)
        menu.findItem(R.id.icons_mono).isChecked = activity.prefs.isIconsMono
        initialized = true
        refresh()
    }

    fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val prefs = WidgetStorage.load(activity, appWidgetId)
        return when (menuItem.itemId) {
            R.id.apply -> {
                prefs.skin = activity.currentSkinItem.value
                prefs.apply()
                activity.beforeFinish()
                activity.finish()
                return true
            }
            R.id.menu_number -> {
                createNumberPickerDialog().show()
                return true
            }
            R.id.tile_color -> {
                val value = prefs.tileColor
                val d = CarHomeColorPickerDialog
                        .newInstance(value!!, true, activity)
                d.listener = ColorPickerSwatch.OnColorSelectedListener { color ->
                    prefs.tileColor = color
                    prefs.apply()
                    showTileColorButton()
                    activity.refreshSkinPreview()
                }
                d.show(activity.supportFragmentManager, "tileColor")
                return true
            }
            R.id.more -> {
                val intent = ConfigurationActivity
                        .createFragmentIntent(activity, ConfigurationLook::class.java)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                activity.startActivityForResult(intent, REQUEST_LOOK_ACTIVITY)
                return true
            }
            R.id.bg_color -> {
                val value = activity.prefs.backgroundColor
                val d = CarHomeColorPickerDialog.newInstance(value, true, activity)
                d.listener = ColorPickerSwatch.OnColorSelectedListener { color ->
                    prefs.backgroundColor = color
                    prefs.apply()
                    activity.refreshSkinPreview()
                }
                d.show(activity.supportFragmentManager, "bgColor")
                return true
            }
            R.id.icons_theme -> {
                val mainIntent = Intent(activity, IconThemesActivity::class.java)
                mainIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                Utils.startActivityForResultSafetly(mainIntent, REQUEST_PICK_ICON_THEME, activity)
                return true
            }
            R.id.icons_mono -> {
                activity.prefs.isIconsMono = !menuItem.isChecked
                activity.persistPrefs()
                menuItem.isChecked = !menuItem.isChecked
                activity.refreshSkinPreview()
                return true
            }
            R.id.icons_scale -> {
                val values = activity.resources.getStringArray(R.array.icon_scale_values)
                val scale = activity.prefs.iconsScale
                val idx = values.indexOf(scale)
                val style = App.theme(activity).alert
                DialogSingleChoice(activity, style, R.string.pref_scale_icon, R.array.icon_scale_titles, idx) {
                    dialog, which ->
                    activity.prefs.setIconsScaleString(values[which])
                    activity.persistPrefs()
                    dialog.dismiss()
                    activity.refreshSkinPreview()
                }.show()
                return true
            }
            R.id.backup -> {
                val intent = ConfigurationActivity.createFragmentIntent(activity, FragmentBackup::class.java)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                activity.startActivity(intent)
                return true
            }
            R.id.skin_info -> {
                val style = App.theme(activity).alert
                DialogMessage(activity, style, R.string.info, HtmlCompat.fromHtml(activity.getString(R.string.skin_info_bbb))) {
                    it.setCancelable(true)
                    it.setPositiveButton(android.R.string.ok) { _, _ ->  }
                }.show()
                return true
            }
            else -> false
        }
    }

    fun refresh() {
        if (initialized) {
            showTileColorButton()
            menuInfo!!.isVisible = activity.currentSkinItem.value == WidgetInterface.SKIN_BBB
        }
    }

    private fun showTileColorButton() {
        if (activity.currentSkinItem.value == WidgetInterface.SKIN_WINDOWS7) {
            val prefs = WidgetStorage.load(activity, appWidgetId)
            val size = activity.resources.getDimension(R.dimen.color_preview_size).toInt()

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val c = Canvas(bitmap)
            c.drawColor(prefs.tileColor!!)
            val d = FastBitmapDrawable(bitmap)

            menuTileColor!!.icon = d
            menuTileColor!!.isVisible = true
        } else {
            menuTileColor!!.isVisible = false
        }
    }


    private fun createNumberPickerDialog(): AlertDialog {
        val nums = activity.resources.getStringArray(R.array.shortcut_numbers)

        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val npView = inflater.inflate(R.layout.numberpicker, null)

        val numberPicker = npView.findViewById<View>(R.id.numberPicker) as NumberPicker
        numberPicker.minValue = 0
        numberPicker.maxValue = nums.size - 1
        numberPicker.displayedValues = nums

        val countStr = model.count.toString()
        for (i in nums.indices) {
            if (countStr == nums[i]) {
                numberPicker.value = i
                break
            }
        }

        val builder = AlertDialog.Builder(activity)
        builder.setView(npView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val value = numberPicker.value
                    model.updateCount(Integer.valueOf(nums[value]))
                    activity.refreshSkinPreview()
                }
                .setNegativeButton(android.R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
                .setTitle(R.string.number_shortcuts_title)
        return builder.create()
    }

    companion object {
        const val REQUEST_LOOK_ACTIVITY = 1
        const val REQUEST_PICK_ICON_THEME = 2
    }
}
