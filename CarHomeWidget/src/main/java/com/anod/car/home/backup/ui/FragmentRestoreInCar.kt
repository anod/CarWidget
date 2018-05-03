package com.anod.car.home.backup.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import com.anod.car.home.R
import com.anod.car.home.backup.gdrive.InCarGDriveBackup
import com.anod.car.home.backup.BackupCodeRender
import com.anod.car.home.backup.BackupTask
import com.anod.car.home.backup.gdrive.GDriveBackup
import com.anod.car.home.backup.PreferencesBackupManager
import com.anod.car.home.backup.RestoreCodeRender
import com.anod.car.home.backup.RestoreTask
import com.anod.car.home.prefs.preferences.ObjectRestoreManager
import com.anod.car.home.utils.*
import info.anodsplace.android.log.AppLog

import java.io.File

/**
 * @author algavris
 * @date 30/07/2016.
 */
class FragmentRestoreInCar : Fragment(), RestoreTask.RestoreTaskListener, BackupTask.BackupTaskListener, GDriveBackup.Listener {

    private var restoreInCar: ImageButton? = null
    private var lastBackupInCar: TextView? = null
    private var uploadInCar: ImageButton? = null

    private val version: Version by lazy { Version(context!!) }
    private val backupManager: PreferencesBackupManager by lazy { restoreFragment.backupManager }
    private val gDriveBackup: GDriveBackup by lazy { InCarGDriveBackup(this, this) }

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
            restoreInCar!!.setOnClickListener { TrialDialogs.buildProOnlyDialog(context).show() }
        } else {
            restoreInCar!!.setOnClickListener {
                val uri: Uri
                if (backupManager.backupIncarFile.exists()) {
                    uri = Uri.fromFile(backupManager.backupIncarFile)
                } else {
                    uri = Uri.fromFile(File(backupManager.backupDir, ObjectRestoreManager.FILE_INCAR_DAT))
                }
                RestoreTask(PreferencesBackupManager.TYPE_INCAR, backupManager, 0,
                        this@FragmentRestoreInCar)
                        .execute(uri)
            }
        }

        setupDownloadUpload()
        updateInCarTime()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        if (!gDriveBackup.isSupported) {
            menu!!.findItem(R.id.menu_download_from_cloud).isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_new_backup -> {
                BackupTask(PreferencesBackupManager.TYPE_INCAR, backupManager, 0,
                        this@FragmentRestoreInCar)
                        .execute()
                return true
            }
            R.id.menu_download_from_cloud -> {
                if (version.isFree) {
                    TrialDialogs.buildProOnlyDialog(context).show()
                } else {
                    gDriveBackup.download()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        if (requestCode == FragmentBackup.DOWNLOAD_INCAR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                val uri = resultData.data
                AppLog.d("Uri: " + uri!!.toString())
                RestoreTask(PreferencesBackupManager.TYPE_INCAR, backupManager, 0, this@FragmentRestoreInCar)
                        .execute(uri)

            }
        } else if (gDriveBackup.checkRequestCode(requestCode)) {
            gDriveBackup.onActivityResult(requestCode, resultCode, resultData)
        }
    }

    override fun onPause() {
        gDriveBackup.disconnect()
        super.onPause()
    }

    private fun setupDownloadUpload() {
        if (gDriveBackup.isSupported) {
            uploadInCar!!.setOnClickListener {
                val incar = backupManager.backupIncarFile
                gDriveBackup.upload("incar-backup" + PreferencesBackupManager.FILE_EXT_JSON, incar)
            }

        } else {
            uploadInCar!!.visibility = View.GONE
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

    override fun onRestorePreExecute(type: Int) {
        restoreFragment.startRefreshAnim()
    }

    override fun onRestoreFinish(type: Int, code: Int) {
        restoreFragment.stopRefreshAnim()
        val res = RestoreCodeRender.render(code)
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }

    override fun onGDriveActionStart() {
        restoreFragment.startRefreshAnim()
    }

    override fun onGDriveDownloadFinish(filename: String) {
        backup()
    }

    override fun onGDriveUploadFinish() {
        onBackupFinish(PreferencesBackupManager.TYPE_INCAR, PreferencesBackupManager.RESULT_DONE)
    }

    override fun onGDriveError() {
        onRestoreFinish(PreferencesBackupManager.TYPE_INCAR, PreferencesBackupManager.ERROR_UNEXPECTED)
    }

    fun backup() {
        if (AppPermissions.isGranted(context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            BackupTask(PreferencesBackupManager.TYPE_INCAR, backupManager, 0,
                    this@FragmentRestoreInCar)
                    .execute()
        } else {
            AppPermissions.request(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), AppPermissions.REQUEST_STORAGE_WRITE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        AppPermissions.onRequestPermissionsResult(requestCode, grantResults, AppPermissions.REQUEST_STORAGE_WRITE, {
            when (it) {
                is Granted -> backup()
                is Denied -> Toast.makeText(context, "Permissions are required", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
