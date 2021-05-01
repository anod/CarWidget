package com.anod.car.home.main

import android.appwidget.AppWidgetManager
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.backup.Backup
import com.anod.car.home.backup.Backup.LEGACY_PATH
import com.anod.car.home.backup.BackupManager
import com.anod.car.home.databinding.FragmentAboutBinding
import com.anod.car.home.prefs.MusicAppSettingsActivity
import com.anod.car.home.prefs.model.AppTheme
import com.anod.car.home.utils.Utils
import com.anod.car.home.utils.forApplicationDetails
import com.anod.car.home.utils.startProgressAnimation
import com.anod.car.home.utils.stopProgressAnimation
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.DialogCustom
import info.anodsplace.framework.app.applicationContext
import info.anodsplace.framework.content.CreateDocument
import info.anodsplace.framework.content.startActivitySafely

class AboutFragment : Fragment() {
    private lateinit var createDocumentLauncherWidget: ActivityResultLauncher<CreateDocument.Args>
    private lateinit var createDocumentLauncherIncar: ActivityResultLauncher<CreateDocument.Args>
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AboutViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { destUri ->
            if (destUri == null) {
                binding.restore.stopProgressAnimation()
            } else {
                viewModel.restore(destUri)
            }
        }

        createDocumentLauncherIncar = registerForActivityResult(CreateDocument()) { destUri ->
            if (destUri == null) {
                binding.backupInCar.stopProgressAnimation()
            } else {
                viewModel.backup(Backup.TYPE_INCAR, destUri)
            }
        }

        createDocumentLauncherWidget = registerForActivityResult(CreateDocument()) { destUri ->
            if (destUri == null) {
                binding.backupWidget.stopProgressAnimation()
            } else {
                viewModel.backup(Backup.TYPE_MAIN, destUri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.appWidgetId = Utils.readAppWidgetId(savedInstanceState, requireActivity().intent)

        val themeIdx = App.theme(requireContext()).themeIdx
        binding.buttonAppTheme.setOnClickListener {
            changeTheme(themeIdx)
        }
        val themes = resources.getStringArray(R.array.app_themes)
        val themeName = themes[themeIdx]
        val themeText = SpannableStringBuilder("${getString(R.string.app_theme)}\n$themeName")
        themeText.setSpan(AbsoluteSizeSpan(8, true), themeText.length - themeName.length, themeText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.buttonAppTheme.text = themeText

        binding.buttonDefaultCarDockApp.setOnClickListener {
            onCarDockAppClick()
        }

        binding.buttonMusicApp.setOnClickListener {
            val musicAppsIntent = Intent(context, MusicAppSettingsActivity::class.java)
            requireContext().startActivity(musicAppsIntent)
        }

        val musicApp = renderMusicApp()
        val musicAppText = SpannableStringBuilder("${getString(R.string.music_app)}\n$musicApp")
        musicAppText.setSpan(AbsoluteSizeSpan(8, true), musicAppText.length - musicApp.length, musicAppText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.buttonMusicApp.text = musicAppText

        binding.buttonVersion.setOnClickListener {
            val uri = Uri.parse(DETAIL_MARKET_URL.format(requireContext().packageName))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            requireContext().startActivitySafely(intent)
        }

        initBackup()

        val ver = renderVersion()
        val versionText = SpannableStringBuilder("$ver\n${getString(R.string.version_summary)}")
        versionText.setSpan(AbsoluteSizeSpan(8, true), ver.length + 1, versionText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.buttonVersion.text = versionText
    }

    private fun changeTheme(themeIdx: Int) {
        val newThemeIdx = if (themeIdx == 0) 1 else 0
        val appSettings = App.provide(requireContext()).appSettings
        appSettings.theme = newThemeIdx
        appSettings.apply()
        val app = App.get(requireContext())
        app.appComponent.theme = AppTheme(newThemeIdx)
        AppCompatDelegate.setDefaultNightMode(app.nightMode)
        requireActivity().setTheme(app.appComponent.theme.mainResource)
        requireActivity().recreate()
    }

    private fun initBackup() {
        viewModel.backupEvent.observe(viewLifecycleOwner, { code ->
            when (code) {
                Backup.NO_RESULT -> {
                }
                else -> {
                    binding.backupInCar.stopProgressAnimation()
                    binding.backupWidget.stopProgressAnimation()
                    Toast.makeText(context, Backup.renderBackupCode(code), Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.restoreEvent.observe(viewLifecycleOwner, { code ->
            when (code) {
                Backup.NO_RESULT -> {
                }
                else -> {
                    binding.restore.stopProgressAnimation()
                    Toast.makeText(context, Backup.renderRestoreCode(code), Toast.LENGTH_SHORT).show()
                    if (code == Backup.RESULT_DONE && activity is BackupManager.OnRestore) {
                        (activity as BackupManager.OnRestore).restoreCompleted()
                    }
                }
            }
        })

        binding.backupInCar.setOnClickListener {
            try {
                it.startProgressAnimation()
                Toast.makeText(context, getString(R.string.backup_path, LEGACY_PATH), Toast.LENGTH_LONG).show()
                val initialUri = FileProvider.getUriForFile(applicationContext().actual, Backup.AUTHORITY, Backup.legacyBackupDir)
                createDocumentLauncherIncar.launch(CreateDocument.Args(initialUri, "application/json", Backup.FILE_INCAR_JSON))
            } catch (e: Exception) {
                AppLog.e(e)
                Toast.makeText(context, "Cannot start activity: ACTION_CREATE_DOCUMENT", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backupWidget.isEnabled = viewModel.appWidgetId > AppWidgetManager.INVALID_APPWIDGET_ID
        binding.backupWidget.setOnClickListener {
            try {
                it.startProgressAnimation()
                Toast.makeText(context, getString(R.string.backup_path, LEGACY_PATH), Toast.LENGTH_LONG).show()
                val initialUri = FileProvider.getUriForFile(applicationContext().actual, Backup.AUTHORITY, Backup.legacyBackupDir)
                createDocumentLauncherWidget.launch(CreateDocument.Args(initialUri, "application/json", "carwidget-${viewModel.appWidgetId}" + Backup.FILE_EXT_JSON))
            } catch (e: Exception) {
                AppLog.e(e)
                Toast.makeText(context, "Cannot start activity: ACTION_CREATE_DOCUMENT", Toast.LENGTH_SHORT).show()
            }
        }

        binding.restore.setOnClickListener {
            it.startProgressAnimation()
            //  addCategory(Intent.CATEGORY_OPENABLE)
            openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        }
    }

    private fun onCarDockAppClick() {
        val style = App.theme(requireContext()).alert
        DialogCustom(requireContext(), style, R.string.default_car_dock_app, R.layout.dialog_car_dock_app) { view, dialog ->
            dialog.setCancelable(true)
            dialog.setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
            view.findViewById<Button>(android.R.id.button1).setOnClickListener {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_CAR_DOCK)
                val info = App.provide(requireContext()).packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                requireContext().startActivitySafely(Intent().forApplicationDetails(info?.activityInfo?.applicationInfo?.packageName
                        ?: ""))
            }
        }.show()
    }

    private fun renderMusicApp(): String {
        val musicAppCmp = App.provide(requireContext()).appSettings.musicApp
        return if (musicAppCmp == null) {
            getString(R.string.show_choice)
        } else {
            try {
                val info = App.provide(requireContext()).packageManager
                        .getApplicationInfo(musicAppCmp.packageName, 0)
                info.loadLabel(App.provide(requireContext()).packageManager).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                AppLog.e(e)
                musicAppCmp.flattenToShortString()
            }

        }
    }

    private fun renderVersion(): String {
        val appName = getString(R.string.app_name)
        var versionName = ""
        try {
            versionName = App.provide(requireContext()).packageManager
                    .getPackageInfo(requireContext().packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            AppLog.e(e)
        }

        return getString(R.string.version_title, appName, versionName)
    }

    companion object {
        private const val DETAIL_MARKET_URL = "market://details?id=%s"
    }

}
