package com.anod.car.home.backup.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.anod.car.home.R
import com.anod.car.home.backup.BackupCodeRender
import com.anod.car.home.backup.PreferencesBackupManager
import com.anod.car.home.backup.RestoreCodeRender
import com.anod.car.home.prefs.preferences.ObjectRestoreManager
import com.anod.car.home.utils.*
import info.anodsplace.framework.AppLog
import kotlinx.android.synthetic.main.fragment_restore_incar.*
import java.io.File

class RestoreInCarViewModel(application: Application) : RestoreViewModel(application) {
    val backupTime = MutableLiveData(0L)

    init {
        appWidgetId = 0
        type = PreferencesBackupManager.TYPE_INCAR
    }

    fun backup() {
        val uri = backupManager.backupIncarFile.toUri()
        backup(uri)
    }

    fun upload() {
        val incar = backupManager.backupIncarFile
        upload("incar-backup" + PreferencesBackupManager.FILE_EXT_JSON, incar)
    }

    fun restore() {
        val uri = if (backupManager.backupIncarFile.exists())
            backupManager.backupIncarFile.toUri()
        else
            File(backupManager.backupDir, ObjectRestoreManager.FILE_INCAR_DAT).toUri()
        restore(uri)
    }

    fun refreshBackupTime() {
        backupTime.value = backupManager.incarTime
    }
}

/**
 * @author algavris
 * @date 30/07/2016.
 */
class FragmentRestoreInCar : Fragment() {

    private val version: Version by lazy { Version(context!!) }

    private val viewModel: RestoreInCarViewModel by viewModels()

    private val restoreFragment: FragmentBackup
        get() = parentFragment as FragmentBackup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_restore_incar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CheatSheet.setup(restoreIncar)
        CheatSheet.setup(uploadIncar)

        viewModel.backupManager = restoreFragment.backupManager

        viewModel.backupTime.observe(this) { time ->
            lastBackupIncar.text = if (time > 0)
                DateUtils.formatDateTime(context, time, FragmentBackup.DATE_FORMAT)
            else
                getString(R.string.never)
        }

        viewModel.backupEvent.observe(this) { code ->
            when (code) {
                PreferencesBackupManager.NO_RESULT -> restoreFragment.startRefreshAnim()
                else -> {
                    if (code == PreferencesBackupManager.RESULT_DONE) {
                        viewModel.refreshBackupTime()
                    }
                    restoreFragment.stopRefreshAnim()
                    Toast.makeText(context, BackupCodeRender.render(code), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.restoreEvent.observe(this) { code ->
            when (code) {
                PreferencesBackupManager.NO_RESULT -> restoreFragment.startRefreshAnim()
                else -> {
                    restoreFragment.stopRefreshAnim()
                    Toast.makeText(context, RestoreCodeRender.render(code), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.uploadEvent.observe(this) { event ->
            upload(event.first, event.second)
        }

        if (version.isFree) {
            restoreIncar.setOnClickListener { TrialDialogs.buildProOnlyDialog(context!!).show() }
        } else {
            restoreIncar.setOnClickListener {
                viewModel.restore()
            }
        }

        uploadIncar.setOnClickListener {
            viewModel.upload()
        }

        if (AppPermissions.isGranted(context!!, ReadExternalStorage)) {
            viewModel.refreshBackupTime()
        } else {
            AppPermissions.request(this, arrayOf(ReadExternalStorage, WriteExternalStorage), requestList)
        }

        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_backup -> {
                viewModel.backup()
                return true
            }
            R.id.menu_download_from_cloud -> {
                if (version.isFree) {
                    TrialDialogs.buildProOnlyDialog(context!!).show()
                } else {
                    download()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        if (requestCode == requestDownload && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let {
                AppLog.d("Uri: $it")
                viewModel.restore(it)
            }
        } else if (requestCode == requestUpload && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let {
                AppLog.d("Uri: $it")
                viewModel.backup(it)
            }
        }
    }

    fun backup() {
        if (AppPermissions.isGranted(context!!, WriteExternalStorage)) {
            viewModel.backup()
        } else {
            AppPermissions.request(this, arrayOf(ReadExternalStorage, WriteExternalStorage), requestBackup)
        }
    }

    private fun download() {
        startActivityForResultSafetly(Intent().forOpenBackup(), FragmentRestoreWidget.requestDownload)
    }

    private fun upload(filename: String, source: File) {
        val uri = FileProvider.getUriForFile(context!!.applicationContext, FragmentRestoreWidget.AUTHORITY, source)
        startActivityForResultSafetly(Intent().forCreateBackup(filename, uri), FragmentRestoreWidget.requestUpload)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AppPermissions.checkResult(requestCode, grantResults, requestBackup) {
            when (it) {
                is Granted -> backup()
                is Denied -> Toast.makeText(context, "Permissions are required", Toast.LENGTH_SHORT).show()
            }
        }

        AppPermissions.checkResult(requestCode, grantResults, requestList) {
            when (it) {
                is Granted -> viewModel.refreshBackupTime()
                is Denied -> Toast.makeText(context, "Permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val requestUpload = 123
        const val requestDownload = 124
        const val requestBackup = 125
        const val requestList = 126
    }
}
