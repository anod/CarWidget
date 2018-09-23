package com.anod.car.home.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.prefs.MusicAppSettingsActivity
import com.anod.car.home.prefs.model.AppTheme
import com.anod.car.home.utils.Utils
import com.anod.car.home.utils.forApplicationDetails
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.DialogCustom
import info.anodsplace.framework.app.DialogSingleChoice
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonAppTheme.setOnClickListener {
            createThemesDialog().show()
        }

        buttonDefaultCarDockApp.setOnClickListener {
            onCarDockAppClick()
        }

        buttonFeedback.setOnClickListener {
            val feedback = Intent(Intent.ACTION_SEND)
            feedback.type = "*/*"
            feedback.putExtra(Intent.EXTRA_EMAIL, arrayOf("alex.gavrishev@gmail.com"))
            feedback.putExtra(Intent.EXTRA_SUBJECT, renderVersion())
            if (feedback.resolveActivity(App.provide(context!!).packageManager) != null) {
                context!!.startActivity(feedback)
            }

        }
        buttonMusicApp.setOnClickListener {
            val musicAppsIntent = Intent(context, MusicAppSettingsActivity::class.java)
            context!!.startActivity(musicAppsIntent)
        }

        val musicApp = renderMusicApp()
        val musicAppText = SpannableStringBuilder("${getString(R.string.music_app)}\n$musicApp")
        musicAppText.setSpan(AbsoluteSizeSpan(8, true), musicAppText.length - musicApp.length, musicAppText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        buttonMusicApp.text = musicAppText

        buttonVersion.setOnClickListener {
            val url = DETAIL_MARKET_URL
            val uri = Uri.parse(String.format(url, context!!.packageName))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            Utils.startActivitySafely(intent, context!!)
        }

        val ver = renderVersion()
        val versionText = SpannableStringBuilder("$ver\n${getString(R.string.version_summary)}")
        versionText.setSpan(AbsoluteSizeSpan(8, true), ver.length+1, versionText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        buttonVersion.text = versionText
    }

    private fun createThemesDialog(): AlertDialog {
        val style = App.theme(context!!).dialog
        return DialogSingleChoice(context!!, style, R.string.choose_a_theme, R.array.app_themes, App.theme(context!!).themeIdx) {
            _, which ->

            val appSettings = App.provide(context!!).appSettings
            appSettings.theme = which
            appSettings.apply()
            val app = App.get(context!!)
            app.appComponent.theme = AppTheme(which)
            AppCompatDelegate.setDefaultNightMode(app.nightMode)
            activity!!.setTheme(app.appComponent.theme.mainResource)
            activity!!.recreate()
        }.create()
    }

    private fun onCarDockAppClick() {
        val style = App.theme(context!!).alert
        DialogCustom(context!!, style, R.string.default_car_dock_app, R.layout.default_car_dock_app) { view, dialog ->

            dialog.setCancelable(true)

            dialog.setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }

            view.findViewById<Button>(android.R.id.button1).setOnClickListener {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_CAR_DOCK)
                val info = App.provide(context!!).packageManager
                        .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                Utils.startActivitySafely(Intent().forApplicationDetails(info.activityInfo.applicationInfo.packageName), context!!)
            }
        }.show()
    }

    private fun renderMusicApp(): String {
        val musicAppCmp = App.provide(context!!).appSettings.musicApp
        return if (musicAppCmp == null) {
            context!!.getString(R.string.show_choice)
        } else {
            try {
                val info = App.provide(context!!).packageManager
                        .getApplicationInfo(musicAppCmp.packageName, 0)
                info.loadLabel(App.provide(context!!).packageManager).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                AppLog.e(e)
                musicAppCmp.flattenToShortString()
            }

        }
    }

    private fun renderVersion(): String {
        val versionText = context!!.getString(R.string.version_title)
        val appName = context!!.getString(R.string.app_name)
        var versionName = ""
        try {
            versionName = App.provide(context!!).packageManager
                    .getPackageInfo(context!!.packageName, 0).versionName
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
