package com.anod.car.home.backup.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.anod.car.home.R
import com.anod.car.home.backup.*
import com.anod.car.home.prefs.preferences.ObjectRestoreManager
import com.anod.car.home.utils.*
import info.anodsplace.framework.AppLog
import java.io.File

/**
 * @author algavris
 * @date 30/07/2016.
 */
class FragmentRestoreInCar : androidx.fragment.app.Fragment(), RestoreTask.RestoreTaskListener, BackupTask.BackupTaskListener {

    private var restoreInCar: ImageButton? = null
    private var lastBackupInCar: TextView? = null
    private var uploadInCar: ImageButton? = null

    private val version: Version by lazy { Version(context!!) }
    private val backupManager: PreferencesBackupManager by lazy { restoreFragment.backupManager }

    private val restoreFragment: FragmentBackup
        get() = parentFragment as FragmentBackup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_restore_incar, container, false)

        restoreInCar = view.findViewById(R.id.restoreIncar)
        lastBackupInCar = view.findViewById(R.id.lastBackupIncar)
        uploadInCar = view.findViewById(R.id.uploadIncar)
        CheatSheet.setup(restoreInCar!!)
        CheatSheet.setup(uploadInCar!!)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (version.isFree) {
            restoreInCar!!.setOnClickListener { TrialDialogs.buildProOnlyDialog(context!!).show() }
        } else {
            restoreInCar!!.setOnClickListener {
                val uri: Uri
                if (backupManager.backupIncarFile.exists()) {
                    uri = backupManager.backupIncarFile.toUri()
                } else {
                    uri = File(backupManager.backupDir, ObjectRestoreManager.FILE_INCAR_DAT).toUri()
                }
                RestoreTask(backupManager, uri, this@FragmentRestoreInCar).execute()
            }
        }

        uploadInCar!!.setOnClickListener {
            val incar = backupManager.backupIncarFile
            upload("incar-backup" + PreferencesBackupManager.FILE_EXT_JSON, incar)
        }

        if (AppPermissions.isGranted(context!!, ReadExternalStorage)) {
            updateInCarTime()
        } else {
            AppPermissions.request(this, arrayOf(ReadExternalStorage, WriteExternalStorage), requestList)
        }

        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_backup -> {
                val uri = backupManager.backupIncarFile.toUri()
                BackupTask(backupManager, uri, this@FragmentRestoreInCar).execute()
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
                RestoreTask(backupManager, it, this@FragmentRestoreInCar).execute()

            }
        } else if (requestCode == requestUpload && resultCode == Activity.RESULT_OK) {
            resultData?.data?.let {
                AppLog.d("Uri: $it")
                BackupTask(backupManager, it, this@FragmentRestoreInCar).execute()
            }
        }
    }

    private fun updateInCarTime() {
        val summary: String
        val timeIncar = backupManager.incarTime
        if (timeIncar > 0) {
            summary = DateUtils.formatDateTime(context, timeIncar, FragmentBackup.DATE_FORMAT)
        } else {
            summary = getString(R.string.never)
        }
        lastBackupInCar!!.text = summary
    }

    override fun onBackupPreExecute(type: Int) {
        restoreFragment.startRefreshAnim()
    }

    override fun onBackupFinish(type: Int, code: Int) {
        if (code == PreferencesBackupManager.RESULT_DONE) {
            updateInCarTime()
        }

        restoreFragment.stopRefreshAnim()
        val res = BackupCodeRender.render(code)
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }

    fun backup() {
        if (AppPermissions.isGranted(context!!, WriteExternalStorage)) {
            BackupTask(backupManager, backupManager.backupIncarFile.toUri(), this@FragmentRestoreInCar)
                    .execute()
        } else {
            AppPermissions.request(this, arrayOf(ReadExternalStorage, WriteExternalStorage), requestBackup)
        }
    }

    override fun onRestorePreExecute(type: Int) {
        restoreFragment.startRefreshAnim()
    }

    override fun onRestoreFinish(type: Int, code: Int) {
        restoreFragment.stopRefreshAnim()
        val res = RestoreCodeRender.render(code)
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }

    private fun download() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val mimeTypes = arrayOf("application/json", "application/octet-stream", "text/plain", "*/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        Utils.startActivityForResultSafetly(intent, FragmentRestoreWidget.requestDownload, this)
    }

    private fun upload(filename: String, source: File) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        val uri = FileProvider.getUriForFile(context!!, FragmentRestoreWidget.AUTHORITY, source)
        intent.setDataAndType(uri, "application/json")
        intent.putExtra(Intent.EXTRA_TITLE, filename)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        Utils.startActivityForResultSafetly(intent, FragmentRestoreWidget.requestUpload, this)
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
                is Granted -> updateInCarTime()
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
