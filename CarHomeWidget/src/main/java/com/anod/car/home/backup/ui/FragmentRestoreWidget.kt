package com.anod.car.home.backup.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.anod.car.home.R
import com.anod.car.home.backup.gdrive.AppWidgetGDriveBackup
import com.anod.car.home.backup.BackupCodeRender
import com.anod.car.home.backup.BackupTask
import com.anod.car.home.backup.gdrive.GDriveBackup
import com.anod.car.home.backup.PreferencesBackupManager
import com.anod.car.home.backup.RestoreCodeRender
import com.anod.car.home.backup.RestoreTask
import info.anodsplace.android.log.AppLog
import com.anod.car.home.utils.CheatSheet
import com.anod.car.home.utils.DeleteFileTask

import java.io.File

/**
 * @author algavris
 * @date 30/07/2016.
 */
class FragmentRestoreWidget : Fragment(), RestoreTask.RestoreTaskListener, DeleteFileTask.DeleteFileTaskListener, BackupTask.BackupTaskListener, GDriveBackup.Listener {

    private var listView: ListView? = null

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val backupManager: PreferencesBackupManager by lazy { restoreFragment.backupManager }
    private val adapter: RestoreAdapter by lazy {
        RestoreAdapter(context!!,
                RestoreClickListener(backupManager, appWidgetId, this),
                DeleteClickListener(this),
                ExportClickListener(backupManager, gDriveBackup),
                gDriveBackup) }
    private val gDriveBackup: GDriveBackup by lazy { AppWidgetGDriveBackup(this, appWidgetId, this) }

    private val restoreFragment: FragmentBackup
        get() = parentFragment as FragmentBackup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_restore_widget, container, false)
        listView = view.findViewById(android.R.id.list)
        listView!!.emptyView = view.findViewById(android.R.id.empty)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appWidgetId = arguments!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)

        listView!!.adapter = adapter
        FileListTask(backupManager, adapter).execute(0)
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
                val defaultFilename = "widget-$appWidgetId"
                createBackupNameDialog(defaultFilename).show()
                return true
            }
            R.id.menu_download_from_cloud -> {
                gDriveBackup.download()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {

        if (requestCode == FragmentBackup.DOWNLOAD_MAIN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                val uri = resultData.data
                AppLog.d("Uri: " + uri!!.toString())
                RestoreTask(PreferencesBackupManager.TYPE_MAIN, backupManager, appWidgetId, this@FragmentRestoreWidget)
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

    override fun onBackupPreExecute(type: Int) {
        restoreFragment.startRefreshAnim()
    }

    override fun onBackupFinish(type: Int, code: Int) {
        if (code == PreferencesBackupManager.RESULT_DONE) {
            FileListTask(backupManager, adapter).execute(0)
        }

        restoreFragment.stopRefreshAnim()
        val res = BackupCodeRender.render(code)
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteFileFinish(success: Boolean) {
        if (!success) {
            Toast.makeText(context, R.string.unable_delete_file, Toast.LENGTH_SHORT).show()
        } else {
            FileListTask(backupManager, adapter).execute(0)
        }
    }

    class ViewHolder(view: View) {
        var title: TextView = view.findViewById(android.R.id.title)
        var text2: TextView = view.findViewById(android.R.id.text2)
        var apply: ImageView = view.findViewById(R.id.apply_icon)
        var delete: ImageView = view.findViewById(R.id.delete_button)
        var export: ImageView = view.findViewById(R.id.uploadMain)
    }

    private class RestoreAdapter constructor(
            context: Context,
            private val restoreListener: RestoreClickListener,
            private val deleteListener: DeleteClickListener,
            private val exportListener: ExportClickListener,
            private val gDriveBackup: GDriveBackup)
        : ArrayAdapter<File>(context, R.layout.fragment_restore_item, emptyArray()) {


        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var itemView = view
            val holder: ViewHolder
            if (itemView != null) {
                holder = itemView.tag as ViewHolder
            } else {
                val inflater = context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                itemView = inflater.inflate(R.layout.fragment_restore_item, parent, false)
                holder = ViewHolder(itemView)
                itemView!!.tag = holder
            }

            val entry = getItem(position)

            val name = entry!!.name
            val title = name.substring(0, name.lastIndexOf("."))
            holder.title.tag = entry.name
            holder.title.text = title
            holder.title.setOnClickListener(restoreListener)

            val timestamp = DateUtils.formatDateTime(context, entry.lastModified(), FragmentBackup.DATE_FORMAT)
            holder.text2.text = timestamp

            holder.apply.tag = name
            holder.apply.setOnClickListener(restoreListener)
            CheatSheet.setup(holder.apply)

            holder.delete.tag = entry
            holder.delete.setOnClickListener(deleteListener)
            CheatSheet.setup(holder.delete)

            if (gDriveBackup.isSupported) {
                holder.export.tag = name
                holder.export.setOnClickListener(exportListener)
                CheatSheet.setup(holder.export)
            } else {
                holder.export.visibility = View.GONE
            }

            itemView.id = position
            return itemView
        }
    }

    private class FileListTask(
            private val backupManager: PreferencesBackupManager,
            private val adapter: RestoreAdapter) : AsyncTask<Int, Void, Array<File>>() {

        override fun onPreExecute() {
            adapter.clear()
            adapter.notifyDataSetChanged()
        }

        override fun doInBackground(vararg params: Int?): Array<File> {
            return backupManager.mainBackups
        }

        override fun onPostExecute(result: Array<File>?) {
            if (result != null) {
                for (i in result.indices) {
                    adapter.add(result[i])
                    adapter.notifyDataSetChanged()
                }
            }
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

    private class RestoreClickListener(
            private val backupManager: PreferencesBackupManager,
            private val appWidgetId: Int,
            private val listener: RestoreTask.RestoreTaskListener) : View.OnClickListener {

        override fun onClick(v: View) {

            val uri = Uri.fromFile(backupManager.getBackupWidgetFile(v.tag as String))
            RestoreTask(PreferencesBackupManager.TYPE_MAIN, backupManager, appWidgetId,
                    listener)
                    .execute(uri)
        }
    }

    private class DeleteClickListener(private val listener: DeleteFileTask.DeleteFileTaskListener) : View.OnClickListener {

        override fun onClick(v: View) {
            val file = v.tag as File
            DeleteFileTask(listener).execute(file)
        }
    }

    private fun createBackupNameDialog(currentName: String): AlertDialog {
        // This example shows how to add a custom layout to an AlertDialog
        val factory = LayoutInflater.from(context)
        val textEntryView = factory.inflate(R.layout.backup_dialog_enter_name, null)
        val backupName = textEntryView.findViewById<View>(R.id.backup_name) as EditText
        backupName.setText(currentName)

        return AlertDialog.Builder(context!!)
                .setTitle(R.string.save_widget)
                .setView(textEntryView)
                .setPositiveButton(R.string.backup_save) { _, _ ->
                    val filename = backupName.text.toString()
                    if (filename.isNotBlank()) {
                        BackupTask(PreferencesBackupManager.TYPE_MAIN, backupManager,
                                appWidgetId, this@FragmentRestoreWidget).execute(filename)
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    //Nothing
                }.create()
    }

    private class ExportClickListener(
            private val backupManager: PreferencesBackupManager,
            private val gDriveBackup: GDriveBackup) : View.OnClickListener {

        override fun onClick(v: View) {
            val name = v.tag as String
            val file = backupManager.getBackupWidgetFile(name)
            gDriveBackup.upload("car-$name", file)
        }
    }

    override fun onGDriveActionStart() {
        restoreFragment.startRefreshAnim()
    }

    override fun onGDriveDownloadFinish(filename: String) {
        var onlyName = filename
        onRestoreFinish(PreferencesBackupManager.TYPE_MAIN, PreferencesBackupManager.RESULT_DONE)

        val pos = onlyName.lastIndexOf('.')
        if (pos > 0) {
            onlyName = onlyName.substring(0, pos)
        }
        createBackupNameDialog(onlyName).show()
    }

    override fun onGDriveUploadFinish() {
        onBackupFinish(PreferencesBackupManager.TYPE_MAIN, PreferencesBackupManager.RESULT_DONE)
    }

    override fun onGDriveError() {
        onRestoreFinish(PreferencesBackupManager.TYPE_MAIN, PreferencesBackupManager.ERROR_UNEXPECTED)
    }

    companion object {

        fun create(appWidgetId: Int): FragmentRestoreWidget {
            val fragment = FragmentRestoreWidget()
            val args = Bundle()
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            fragment.arguments = args
            return fragment
        }
    }
}