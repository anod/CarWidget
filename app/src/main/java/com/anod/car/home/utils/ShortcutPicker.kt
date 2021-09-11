package com.anod.car.home.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.anod.car.home.R
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.content.db.Shortcuts
import com.anod.car.home.prefs.*
import info.anodsplace.carwidget.content.shortcuts.ShortcutInfoUtils
import java.util.*

/**
 * @author alex
 * @date 2014-10-24
 */
class ShortcutPicker(private val model: Shortcuts, private val handler: Handler, private val context: Context) {
    private var currentCellId = INVALID_CELL_ID

    interface Handler {
        fun startActivityForResult(intent: Intent, requestCode: Int)
        fun onAddShortcut(cellId: Int, info: Shortcut?, resultCode: Int)
        fun onEditComplete(cellId: Int)
    }

    fun showActivityPicker(position: Int) {
        val pickIntent = createPickIntent(position)
        handler.startActivityForResult(pickIntent, REQUEST_PICK_SHORTCUT)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_APPLICATION -> return completeAddShortcut(data, true)
                REQUEST_CREATE_SHORTCUT -> return completeAddShortcut(data, false)
                REQUEST_PICK_SHORTCUT -> return pickShortcut(data!!)
            }
        }
        return false
    }

    private fun createPickIntent(position: Int): Intent {
        val bundle = Bundle()

        val shortcutNames = ArrayList<String>()

        shortcutNames.add(context.getString(R.string.applications))
        shortcutNames.add(context.getString(R.string.car_widget_shortcuts))
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames)

        val shortcutIcons = ArrayList<Intent.ShortcutIconResource>()
        shortcutIcons.add(Intent.ShortcutIconResource
                .fromContext(context, R.drawable.ic_launcher_application))
        shortcutIcons.add(Intent.ShortcutIconResource
                .fromContext(context, R.drawable.ic_launcher_carwidget))

        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons)

        val dataIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
        dataIntent.putExtra(EXTRA_CELL_ID, position)

        val pickIntent = Intent(context, ActivityPicker::class.java)
        pickIntent.putExtras(bundle)
        pickIntent.putExtra(Intent.EXTRA_INTENT, dataIntent)
        pickIntent.putExtra(Intent.EXTRA_TITLE, context.getString(R.string.select_shortcut_title))
        return pickIntent
    }

    private fun pickShortcut(intent: Intent): Boolean {
        // Handle case where user selected "Applications"
        val applicationName = context.getString(R.string.applications)
        val shortcutsName = context.getString(R.string.car_widget_shortcuts)

        val shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
        currentCellId = intent.getIntExtra(EXTRA_CELL_ID, INVALID_CELL_ID)
        when {
            applicationName == shortcutName -> {
                val mainIntent = Intent(context, AllAppsActivity::class.java)
                startActivityForResultSafely(mainIntent, REQUEST_PICK_APPLICATION)
            }
            shortcutsName == shortcutName -> {
                val mainIntent = Intent(context, CarWidgetShortcutsPicker::class.java)
                startActivityForResultSafely(mainIntent, REQUEST_CREATE_SHORTCUT)
            }
            else -> startActivityForResultSafely(intent, REQUEST_CREATE_SHORTCUT)
        }
        return true
    }

    private fun startActivityForResultSafely(intent: Intent, requestCode: Int) {
        try {
            handler.startActivityForResult(intent, requestCode)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.activity_not_found),
                    Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(context, context.getString(R.string.activity_not_found),
                    Toast.LENGTH_SHORT).show()
            Log.e("CarHomeWidget", "Widget does not have the permission to launch " + intent
                    + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                    + "or use the exported attribute for this activity.", e)
        }

    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("currentCellId", currentCellId)
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?, intent: Intent?): Int {
        if (savedInstanceState != null) {
            currentCellId = savedInstanceState.getInt("currentCellId", INVALID_CELL_ID)
            return currentCellId
        } else if (intent != null) {
            val extras = intent.extras
            if (extras != null) {
                currentCellId = extras.getInt(EXTRA_CELL_ID, INVALID_CELL_ID)
                return currentCellId
            }
        }
        return INVALID_CELL_ID
    }

    private fun completeAddShortcut(data: Intent?, isApplicationShortcut: Boolean): Boolean {
        if (currentCellId == INVALID_CELL_ID || data == null) {
            return false
        }

        val result = model.saveIntent(currentCellId, data, isApplicationShortcut)
        handler.onAddShortcut(currentCellId, result.first, result.second)
        if (result.second == ShortcutInfoUtils.successAppShortcut) {
            Toast.makeText(context, R.string.app_shortcuts_limited, Toast.LENGTH_SHORT).show()
        } else if (result.second == ShortcutInfoUtils.failedAppShortcut) {
            Toast.makeText(context, R.string.app_shortcuts_limited, Toast.LENGTH_LONG).show()
        }
        currentCellId = INVALID_CELL_ID
        return true
    }

    companion object {
        private const val REQUEST_PICK_SHORTCUT = 2
        private const val REQUEST_PICK_APPLICATION = 3
        private const val REQUEST_CREATE_SHORTCUT = 4
        const val EXTRA_CELL_ID = "CarHomeWidgetCellId"
        const val INVALID_CELL_ID = -1
    }
}
