package com.anod.car.home.main

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.backup.Backup
import com.anod.car.home.backup.Backup.LEGACY_PATH
import com.anod.car.home.prefs.MusicAppSettingsActivity
import com.anod.car.home.prefs.model.AppTheme
import com.anod.car.home.utils.*
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.DialogCustom
import info.anodsplace.framework.app.DialogSingleChoice
import info.anodsplace.framework.app.applicationContext
import info.anodsplace.framework.app.startActivityForResultSafely
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {
    private val viewModel: AboutViewModel by viewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.appWidgetId = Utils.readAppWidgetId(savedInstanceState, requireActivity().intent)

        buttonAppTheme.setOnClickListener {
            createThemesDialog().show()
        }

        buttonDefaultCarDockApp.setOnClickListener {
            onCarDockAppClick()
        }

        buttonMusicApp.setOnClickListener {
            val musicAppsIntent = Intent(context, MusicAppSettingsActivity::class.java)
            requireContext().startActivity(musicAppsIntent)
        }

        val musicApp = renderMusicApp()
        val musicAppText = SpannableStringBuilder("${getString(R.string.music_app)}\n$musicApp")
        musicAppText.setSpan(AbsoluteSizeSpan(8, true), musicAppText.length - musicApp.length, musicAppText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        buttonMusicApp.text = musicAppText

        buttonVersion.setOnClickListener {
            val url = DETAIL_MARKET_URL
            val uri = Uri.parse(String.format(url, requireContext().packageName))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            requireContext().startActivitySafely(intent)
        }

        initBackup()

        val ver = renderVersion()
        val versionText = SpannableStringBuilder("$ver\n${getString(R.string.version_summary)}")
        versionText.setSpan(AbsoluteSizeSpan(8, true), ver.length+1, versionText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        buttonVersion.text = versionText
    }

    private fun initBackup() {
        viewModel.backupEvent.observe(viewLifecycleOwner) { code ->
            when (code) {
                Backup.NO_RESULT -> { }
                else -> {
                    backupInCar.stopProgressAnimation()
                    backupWidget.stopProgressAnimation()
                    Toast.makeText(context, Backup.renderBackupCode(code), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.restoreEvent.observe(viewLifecycleOwner) { code ->
            when (code) {
                Backup.NO_RESULT -> { }
                else -> {
                    restore.stopProgressAnimation()
                    Toast.makeText(context, Backup.renderRestoreCode(code), Toast.LENGTH_SHORT).show()
                }
            }
        }

        backupInCar.setOnClickListener {
            try {
                it.startProgressAnimation()
                Toast.makeText(context, getString(R.string.backup_path, LEGACY_PATH), Toast.LENGTH_LONG).show()
                startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    val uri = FileProvider.getUriForFile(applicationContext, Backup.AUTHORITY, Backup.legacyBackupDir)
                    setDataAndType(uri, "application/json")
                    putExtra(Intent.EXTRA_TITLE, Backup.FILE_INCAR_JSON)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                    }
                }, Backup.requestBackupInCar)
            } catch (e: Exception) {
                AppLog.e(e)
                Toast.makeText(context, "Cannot start activity: ACTION_CREATE_DOCUMENT", Toast.LENGTH_SHORT).show()
            }
        }

        backupWidget.isEnabled = viewModel.appWidgetId > AppWidgetManager.INVALID_APPWIDGET_ID
        backupWidget.setOnClickListener {
            try {
                it.startProgressAnimation()
                Toast.makeText(context, getString(R.string.backup_path, LEGACY_PATH), Toast.LENGTH_LONG).show()
                startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    val uri = FileProvider.getUriForFile(applicationContext, Backup.AUTHORITY, Backup.legacyBackupDir)
                    setDataAndType(uri, "application/json")
                    putExtra(Intent.EXTRA_TITLE, "widget-${viewModel.appWidgetId}" + Backup.FILE_EXT_JSON)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                    }
                }, Backup.requestBackupWidget)
            } catch (e: Exception) {
                AppLog.e(e)
                Toast.makeText(context, "Cannot start activity: ACTION_CREATE_DOCUMENT", Toast.LENGTH_SHORT).show()
            }
        }

        restore.setOnClickListener {
            it.startProgressAnimation()
            startActivityForResultSafely(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/plain", "*/*"))
            }, Backup.requestRestore)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == Backup.requestBackupWidget && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let { viewModel.backup(Backup.TYPE_MAIN, it) }
        } else if (requestCode == Backup.requestBackupInCar && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let { viewModel.backup(Backup.TYPE_INCAR, it) }
        } else if (requestCode == Backup.requestRestore && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let { viewModel.restore(it) }
        }
    }

    private fun createThemesDialog(): AlertDialog {
        val style = App.theme(requireContext()).dialog
        return DialogSingleChoice(requireContext(), style, R.string.choose_a_theme, R.array.app_themes, App.theme(requireContext()).themeIdx) { d, which ->

            context?.also {
                val appSettings = App.provide(it).appSettings
                appSettings.theme = which
                appSettings.apply()
                val app = App.get(it)
                app.appComponent.theme = AppTheme(which)
                AppCompatDelegate.setDefaultNightMode(app.nightMode)
                requireActivity().setTheme(app.appComponent.theme.mainResource)
                requireActivity().recreate()
            }
            d.dismiss()

        }.create()
    }

    private fun onCarDockAppClick() {
        val style = App.theme(requireContext()).alert
        DialogCustom(requireContext(), style, R.string.default_car_dock_app, R.layout.default_car_dock_app) { view, dialog ->

            dialog.setCancelable(true)

            dialog.setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }

            view.findViewById<Button>(android.R.id.button1).setOnClickListener {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_CAR_DOCK)
                val info = App.provide(requireContext()).packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                requireContext().startActivitySafely(Intent().forApplicationDetails(info.activityInfo.applicationInfo.packageName))
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
    }

}
