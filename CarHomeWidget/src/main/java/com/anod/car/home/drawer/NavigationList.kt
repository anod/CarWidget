package com.anod.car.home.drawer

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.widget.Button

import com.anod.car.home.MainActivity
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.prefs.ConfigurationActivity
import com.anod.car.home.prefs.ConfigurationInCar
import com.anod.car.home.backup.ui.FragmentBackup
import com.anod.car.home.prefs.LookAndFeelActivity
import com.anod.car.home.prefs.MusicAppSettingsActivity
import com.anod.car.home.prefs.model.AppSettings
import com.anod.car.home.prefs.model.AppTheme
import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.InCarStatus
import com.anod.car.home.utils.IntentUtils
import com.anod.car.home.utils.Utils
import info.anodsplace.framework.app.DialogCustom
import info.anodsplace.framework.app.DialogSingleChoice

import java.util.ArrayList
import android.support.v4.content.ContextCompat.startActivity




/**
 * @author alex
 * @date 2014-10-21
 */
class NavigationList(private val context: Activity, private val appWidgetId: Int) : ArrayList<NavigationList.Item>() {

    private val packageManager: PackageManager = context.packageManager

    open class Item(internal var id: Int, var titleRes: Int, var titleText: String)

    class TitleItem(titleRes: Int) : Item(0, titleRes, "")

    class ActionItem(id: Int,
                     @StringRes titleRes: Int,
                     @param:StringRes internal var summaryRes: Int,
                     @param:DrawableRes internal var iconRes: Int)
        : Item(id, titleRes, "") {

        var summaryText: String = ""

        constructor(id: Int, title: String, @StringRes summaryRes: Int,
                    @DrawableRes iconRes: Int) : this(id, 0, summaryRes, iconRes) {
            titleText = title
        }

        constructor(id: Int, @StringRes titleRes: Int, summary: String,
                    @DrawableRes iconRes: Int) : this(id, titleRes, 0, iconRes) {
            this.summaryText = summary
        }
    }

    private fun addDefaults() {

        val active = context.getString(InCarStatus.render(context))

        if (appWidgetId > 0) {
            addTitle(R.string.current_widget)
            addButton(ID_CURRENT_WIDGET, R.string.shortcuts_and_look,
                    R.string.shortcuts_and_look_summary, R.drawable.ic_now_widgets_white_24dp)
            addButton(ID_BACKUP, R.string.pref_backup_title, R.string.pref_backup_summary,
                    R.drawable.ic_backup_white_24dp)
        }

        addTitle(0)
        val incarSummary = context.getString(R.string.settings) + ". " + active
        addButton(ID_CAR_SETTINGS, R.string.pref_incar_mode_title, incarSummary,
                R.drawable.ic_settings_white_24dp)
        addButton(ID_WIDGETS, R.string.widgets, R.string.list_of_active_widgets,
                R.drawable.ic_now_widgets_white_24dp)

        addTitle(0)
        val themeNameRes = AppTheme
                .getNameResource(App.get(context).themeIdx)
        addButton(ID_THEME, R.string.app_theme, themeNameRes,
                R.drawable.ic_invert_colors_on_white_24dp)

        val musicApp = renderMusicApp()
        addButton(ID_MUSIC_APP, R.string.music_app, musicApp, R.drawable.ic_headset_white_24dp)

        val carDockApp = renderCarDockApp()
        addButton(ID_CAR_DOCK_APP, R.string.default_car_dock_app, carDockApp,
                R.drawable.ic_android_white_24dp)

        addTitle(R.string.information_title)
        val versionTitle = renderVersion()
        addButton(ID_VERSION, versionTitle, R.string.version_summary,
                R.drawable.ic_stars_white_24dp)
        addButton(ID_FEEDBACK, R.string.issue_title, 0, R.drawable.ic_email_black_24dp)

    }

    fun refresh() {
        clear()
        addDefaults()
    }

    private fun renderCarDockApp(): String {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_CAR_DOCK)
        val info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return if (info == null || info.activityInfo.name == RESOLVER_ACTIVITY) {
            context.getString(R.string.not_set)
        } else info.loadLabel(packageManager).toString()
    }

    private fun addButton(id: Int, titleRes: Int, summaryRes: Int, iconRes: Int) {
        add(ActionItem(id, titleRes, summaryRes, iconRes))
    }

    private fun addButton(id: Int, titleRes: Int, summary: String, iconRes: Int) {
        add(ActionItem(id, titleRes, summary, iconRes))
    }

    private fun addButton(id: Int, title: String, summaryRes: Int, iconRes: Int) {
        add(ActionItem(id, title, summaryRes, iconRes))
    }

    private fun addTitle(titleId: Int) {
        add(TitleItem(titleId))
    }

    fun onClick(id: Int): Boolean {
        val intent: Intent
        when (id) {
            ID_CURRENT_WIDGET -> {
                if (context is LookAndFeelActivity) {
                    return true
                }
                context.finish()
                return false
            }
            ID_WIDGETS -> {
                if (context is MainActivity) {
                    return true
                }
                context.startActivity(Intent(context, MainActivity::class.java))
                return true
            }
            ID_CAR_SETTINGS -> {
                intent = ConfigurationActivity.createFragmentIntent(context, ConfigurationInCar::class.java)
                context.startActivity(intent)
                return false
            }
            ID_CAR_DOCK_APP -> {
                onCarDockAppClick()
                return false
            }
            ID_MUSIC_APP -> {
                val musicAppsIntent = Intent(context, MusicAppSettingsActivity::class.java)
                context.startActivity(musicAppsIntent)
                return false
            }
            ID_VERSION -> {
                val url = DETAIL_MARKET_URL
                val uri = Uri.parse(String.format(url, context.packageName))
                intent = Intent(Intent.ACTION_VIEW, uri)
                Utils.startActivitySafely(intent, context)
                return true
            }
            ID_FEEDBACK -> {
                val feedback = Intent(Intent.ACTION_SEND)
                feedback.type = "*/*"
                feedback.putExtra(Intent.EXTRA_EMAIL, arrayOf("alex.gavrishev@gmail.com"))
                feedback.putExtra(Intent.EXTRA_SUBJECT, renderVersion())
                if (feedback.resolveActivity(context.packageManager) != null) {
                    context.startActivity(feedback)
                }
                return true
            }
            ID_THEME -> {
                createThemesDialog().show()
                return false
            }
            ID_BACKUP -> {
                intent = ConfigurationActivity.createFragmentIntent(context, FragmentBackup::class.java)
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                context.startActivity(intent)
                return true
            }
        }
        return true
    }


    private fun createThemesDialog(): AlertDialog {
        return DialogSingleChoice(context, 0, R.string.choose_a_theme, R.array.app_themes, App.get(context).themeIdx, {
            _, which ->

            val appSettings = AppSettings.create(context)
            appSettings.setAppTheme(which)
            appSettings.apply()
            App.get(context).themeIdx = which
            context.setTheme(AppTheme.getMainResource(which))
            context.recreate()

        }).create()
    }

    private fun onCarDockAppClick() {
        DialogCustom(context, 0, R.string.default_car_dock_app, R.layout.default_car_dock_app, {
            view, dialog ->

            dialog.setCancelable(true)

            dialog.setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }

            view.findViewById<Button>(android.R.id.button1).setOnClickListener {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_CAR_DOCK)
                val info = packageManager
                        .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                Utils.startActivitySafely(IntentUtils.createApplicationDetailsIntent(info.activityInfo.applicationInfo.packageName), context)
            }
        }).show()
    }

    private fun renderMusicApp(): String {
        val musicAppCmp = AppSettings.create(context).musicApp
        return if (musicAppCmp == null) {
            context.getString(R.string.show_choice)
        } else {
            try {
                val info = packageManager
                        .getApplicationInfo(musicAppCmp.packageName, 0)
                info.loadLabel(packageManager).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                AppLog.e(e)
                musicAppCmp.flattenToShortString()
            }

        }
    }

    private fun renderVersion(): String {
        val versionText = context.getString(R.string.version_title)
        val appName = context.getString(R.string.app_name)
        var versionName = ""
        try {
            versionName = packageManager
                    .getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            AppLog.e(e)
        }

        return String.format(versionText, appName, versionName)
    }

    companion object {

        private const val DETAIL_MARKET_URL = "market://details?id=%s"

        private const val URL_GOOGLE_PLUS = "https://plus.google.com/communities/106765737887289122631"

        private const val RESOLVER_ACTIVITY = "com.android.internal.app.ResolverActivity"

        const val ID_CAR_SETTINGS = 1
        const val ID_WIDGETS = 7
        internal const val ID_CAR_DOCK_APP = 2
        internal const val ID_THEME = 3
        internal const val ID_MUSIC_APP = 4
        internal const val ID_VERSION = 5
        internal const val ID_FEEDBACK = 6
        const val ID_BACKUP = 8
        const val ID_CURRENT_WIDGET = 9
    }


}
