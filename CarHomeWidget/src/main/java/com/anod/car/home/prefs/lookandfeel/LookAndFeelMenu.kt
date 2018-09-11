package com.anod.car.home.prefs.lookandfeel

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.NumberPicker

import com.anod.car.home.R
import com.anod.car.home.backup.ui.FragmentBackup
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.prefs.ConfigurationActivity
import com.anod.car.home.prefs.ConfigurationLook
import com.anod.car.home.prefs.LookAndFeelActivity
import com.anod.car.home.prefs.colorpicker.CarHomeColorPickerDialog
import com.anod.car.home.prefs.model.WidgetSettings
import com.anod.car.home.prefs.model.WidgetStorage
import com.anod.car.home.utils.FastBitmapDrawable
import com.anod.car.home.utils.Utils

/**
 * @author alex
 * @date 2014-10-20
 */
class LookAndFeelMenu(private val activity: LookAndFeelActivity, private val model: WidgetShortcutsModel) {

    private val appWidgetId: Int = activity.appWidgetId
    private var menuTileColor: MenuItem? = null
    private var initialized: Boolean = false

    fun onCreateOptionsMenu(menu: Menu) {
        val menuInflater = activity.menuInflater
        menuInflater.inflate(R.menu.look_n_feel, menu)

        menuTileColor = menu.findItem(R.id.tile_color)
        menu.findItem(R.id.icons_mono).isChecked = activity.prefs.isIconsMono
        initialized = true
        refreshTileColorButton()
    }

    fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        val prefs = WidgetStorage.load(activity, appWidgetId)
        val itemId = menuItem.itemId
        if (itemId == R.id.apply) {
            prefs.skin = activity.currentSkinItem.value
            prefs.apply()
            activity.beforeFinish()
            activity.finish()
            return true
        }
        if (menuItem.itemId == R.id.menu_number) {
            createNumberPickerDialog().show()
            return true
        }
        if (itemId == R.id.tile_color) {
            val value = prefs.tileColor
            val d = CarHomeColorPickerDialog
                    .newInstance(value!!, true, activity)
            d.setOnColorSelectedListener { color ->
                prefs.tileColor = color
                prefs.apply()
                showTileColorButton()
                activity.refreshSkinPreview()
            }
            d.show(activity.supportFragmentManager, "tileColor")
            return true
        }
        if (itemId == R.id.more) {
            val intent = ConfigurationActivity
                    .createFragmentIntent(activity, ConfigurationLook::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            activity.startActivityForResult(intent, REQUEST_LOOK_ACTIVITY)
            return true
        }
        if (itemId == R.id.bg_color) {
            val value = activity.prefs.backgroundColor
            val d = CarHomeColorPickerDialog
                    .newInstance(value, true, activity)
            d.setOnColorSelectedListener { color ->
                prefs.backgroundColor = color
                prefs.apply()
                activity.refreshSkinPreview()
            }
            d.show(activity.supportFragmentManager, "bgColor")
            return true
        }
        if (itemId == R.id.icons_theme) {
            val mainIntent = Intent(activity, IconThemesActivity::class.java)
            mainIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            Utils.startActivityForResultSafetly(mainIntent, REQUEST_PICK_ICON_THEME, activity)

            return true
        }
        if (itemId == R.id.icons_mono) {
            activity.prefs.isIconsMono = !menuItem.isChecked
            activity.persistPrefs()
            menuItem.isChecked = !menuItem.isChecked
            activity.refreshSkinPreview()
            return true
        }
        if (itemId == R.id.icons_scale) {
            val builder = AlertDialog.Builder(activity)
            val titles = activity.resources
                    .getStringArray(R.array.icon_scale_titles)
            val values = activity.resources
                    .getStringArray(R.array.icon_scale_values)
            var idx = -1
            for (i in values.indices) {
                if (activity.prefs.iconsScale == values[i]) {
                    idx = i
                    break
                }
            }
            builder.setTitle(R.string.pref_scale_icon)
            builder.setSingleChoiceItems(titles, idx) { dialog, item ->
                activity.prefs.setIconsScaleString(values[item])
                activity.persistPrefs()
                dialog.dismiss()
                activity.refreshSkinPreview()
            }
            builder.create().show()
            return true
        }
        if (itemId == R.id.backup) {
            val intent = ConfigurationActivity.createFragmentIntent(activity, FragmentBackup::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            activity.startActivity(intent)
        }
        return false
    }

    fun refreshTileColorButton() {
        if (initialized) {
            showTileColorButton()
        }
    }

    private fun showTileColorButton() {
        if (activity.currentSkinItem.value == WidgetSettings.SKIN_WINDOWS7) {
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

        val inflater = activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
