package com.anod.car.home.backup.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.anod.car.home.BuildConfig
import com.anod.car.home.R
import com.anod.car.home.backup.*
import com.anod.car.home.utils.*
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.DialogCustom
import java.io.File

/**
 * @author algavris
 * @date 30/07/2016.
 */
class FragmentRestoreWidget : androidx.fragment.app.Fragment(), RestoreTask.RestoreTaskListener, DeleteFileTask.DeleteFileTaskListener, BackupTask.BackupTaskListener {

    private var listView: ListView? = null

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val backupManager: PreferencesBackupManager by lazy { restoreFragment.backupManager }
    private val adapter: RestoreAdapter by lazy {
        RestoreAdapter(context!!,
                RestoreClickListener(backupManager, appWidgetId, this),
                DeleteClickListener(this),
                ExportClickListener(backupManager, this)) }

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
        if (AppPermissions.isGranted(context!!, ReadExternalStorage)) {
            FileListTask(backupManager, adapter).execute(0)
        } else {
            AppPermissions.request(this, arrayOf(ReadExternalStorage, WriteExternalStorage), requestList)
        }
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_new_backup -> {
                backup()
                return true
            }
            R.id.menu_download_from_cloud -> {
                download()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun backup() {
        if (AppPermissions.isGranted(context!!, WriteExternalStorage)) {
            createBackupNameDialog("widget-$appWidgetId").show()
        } else {
            AppPermissions.request(this, arrayOf(ReadExternalStorage, WriteExternalStorage), requestBackup)
        }
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
                is Granted -> FileListTask(backupManager, adapter).execute(0)
                is Denied -> Toast.makeText(context, "Permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (requestCode == requestUpload) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = result!!.data
                AppLog.d("Uri: " + uri!!.toString())
                BackupTask(backupManager, appWidgetId, uri, this).execute()
            }
        } else if (requestCode == requestDownload) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = result!!.data
                AppLog.d("Uri: " + uri!!.toString())
                RestoreTask(backupManager, appWidgetId, uri, this@FragmentRestoreWidget).execute()
            }
        }
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
            private val exportListener: ExportClickListener)
        : ArrayAdapter<File>(context, R.layout.fragment_restore_item) {

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

            holder.apply.tag = entry.toUri()
            holder.apply.setOnClickListener(restoreListener)
            CheatSheet.setup(holder.apply)

            holder.delete.tag = entry
            holder.delete.setOnClickListener(deleteListener)
            CheatSheet.setup(holder.delete)

            holder.export.tag = name
            holder.export.setOnClickListener(exportListener)
            CheatSheet.setup(holder.export)

            itemView.id = position
            return itemView
        }
    }

    private class FileListTask(
            private val backupManager: PreferencesBackupManager,
            private val adapter: RestoreAdapter) : AsyncTask<Int, Void, Array<File>>() {

        override fun doInBackground(vararg params: Int?): Array<File> {
            return backupManager.mainBackups
        }

        override fun onPostExecute(result: Array<File>) {
            adapter.clear()
            adapter.addAll(result.toList())
            adapter.notifyDataSetChanged()
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
            RestoreTask(PreferencesBackupManager.TYPE_MAIN, backupManager, appWidgetId, v.tag as Uri,
                    listener)
                    .execute()
        }
    }

    private class DeleteClickListener(private val listener: DeleteFileTask.DeleteFileTaskListener) : View.OnClickListener {

        override fun onClick(v: View) {
            DeleteFileTask(listener).execute(v.tag as File)
        }
    }

    private fun createBackupNameDialog(currentName: String): AlertDialog {
        // This example shows how to add a custom layout to an AlertDialog
        return DialogCustom(context!!, 0, R.string.save_widget, R.layout.backup_dialog_enter_name) {
            view, builder ->

            val backupName = view.findViewById<EditText>(R.id.backup_name)
            backupName.setText(currentName)

            builder.setPositiveButton(R.string.backup_save) { _, _ ->
                val filename = backupName.text.toString()
                if (filename.isNotBlank()) {
                    BackupTask(backupManager, appWidgetId, backupManager.getBackupWidgetFile(filename).toUri(), this@FragmentRestoreWidget)
                            .execute()
                }
            }

            builder.setNegativeButton(android.R.string.cancel) { _, _ -> }

        }.create()
    }

    private class ExportClickListener(
            private val backupManager: PreferencesBackupManager,
            private val fragment: FragmentRestoreWidget) : View.OnClickListener {

        override fun onClick(v: View) {
            val name = v.tag as String
            val file = backupManager.getBackupWidgetFile(name)
            fragment.upload("car-$name", file)
        }
    }

    private fun download() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        val mimeTypes = arrayOf("application/json", "application/octet-stream", "text/plain", "*/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        Utils.startActivityForResultSafetly(intent, requestDownload, this)
    }

    private fun upload(filename: String, source: File) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        val uri = FileProvider.getUriForFile(context!!.applicationContext, AUTHORITY, source)

        intent.setDataAndType(uri, "application/json")
        intent.putExtra(Intent.EXTRA_TITLE, filename)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        Utils.startActivityForResultSafetly(intent, requestUpload, this)
    }

    companion object {

        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"

        const val requestUpload = 123
        const val requestDownload = 124
        const val requestBackup = 125
        const val requestList = 126

        fun create(appWidgetId: Int): FragmentRestoreWidget {
            val fragment = FragmentRestoreWidget()
            val args = Bundle()
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            fragment.arguments = args
            return fragment
        }
    }
}