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
class NavigationDrawerSelection(private val context: Activity, private val appWidgetId: Int) {
    private val packageManager = context.packageManager

    fun onClick(id: Int): Boolean {
        val intent: Intent
        when (id) {
            R.id.nav_current_widget -> {
                if (context is LookAndFeelActivity) {
                    return true
                }
                context.finish()
                return false
            }
            R.id.nav_widgets -> {
                if (context is MainActivity) {
                    return true
                }
                context.startActivity(Intent(context, MainActivity::class.java))
                return true
            }
            R.id.nav_car_settings -> {
                intent = ConfigurationActivity.createFragmentIntent(context, ConfigurationInCar::class.java)
                context.startActivity(intent)
                return false
            }
            R.id.nav_car_dock_app -> {
                onCarDockAppClick()
                return false
            }
            R.id.nav_music_app -> {
                val musicAppsIntent = Intent(context, MusicAppSettingsActivity::class.java)
                context.startActivity(musicAppsIntent)
                return false
            }
            R.id.nav_version -> {
                val url = DETAIL_MARKET_URL
                val uri = Uri.parse(String.format(url, context.packageName))
                intent = Intent(Intent.ACTION_VIEW, uri)
                Utils.startActivitySafely(intent, context)
                return true
            }
            R.id.nav_feedback -> {
                val feedback = Intent(Intent.ACTION_SEND)
                feedback.type = "*/*"
                feedback.putExtra(Intent.EXTRA_EMAIL, arrayOf("alex.gavrishev@gmail.com"))
                feedback.putExtra(Intent.EXTRA_SUBJECT, renderVersion())
                if (feedback.resolveActivity(packageManager) != null) {
                    context.startActivity(feedback)
                }
                return true
            }
            R.id.nav_theme -> {
                createThemesDialog().show()
                return false
            }
            R.id.nav_backup -> {
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
    }
}
