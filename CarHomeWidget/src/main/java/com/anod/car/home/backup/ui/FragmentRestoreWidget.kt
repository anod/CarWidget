package com.anod.car.home.backup.ui

import android.app.Activity
import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.anod.car.home.BuildConfig
import com.anod.car.home.R
import com.anod.car.home.backup.BackupCodeRender
import com.anod.car.home.backup.PreferencesBackupManager
import com.anod.car.home.backup.RestoreCodeRender
import com.anod.car.home.utils.*
import info.anodsplace.framework.AppLog
import info.anodsplace.framework.app.DialogCustom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private class RestoreWidgetViewModel(application: Application) : RestoreViewModel(application) {
    init {
        type = PreferencesBackupManager.TYPE_MAIN
    }

    fun backup(filename: String) {
        backup(backupManager.getBackupWidgetFile(filename).toUri())
    }

    fun upload(name: String) {
        val file = backupManager.getBackupWidgetFile(name)
        upload(name, file)
    }

    override suspend fun refreshFiles(): List<File> = withContext(Dispatchers.IO) {
        backupManager.mainBackups.toList()
    }
}

/**
 * @author algavris
 * @date 30/07/2016.
 */
class FragmentRestoreWidget : Fragment() {

    private var listView: ListView? = null

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val viewModel: RestoreWidgetViewModel by viewModels()
    private val adapter: RestoreAdapter by lazy {
        RestoreAdapter(context!!,
                View.OnClickListener { viewModel.restore(it.tag as Uri) },
                View.OnClickListener { viewModel.delete(it.tag as File) },
                View.OnClickListener { viewModel.upload(it.tag as String) }
        )
    }

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

        viewModel.appWidgetId = arguments!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
        viewModel.backupManager = restoreFragment.backupManager

        viewModel.uploadEvent.observe(this) {
            upload(it.first, it.second)
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

        viewModel.backupEvent.observe(this) { code ->
            when (code) {
                PreferencesBackupManager.NO_RESULT -> restoreFragment.startRefreshAnim()
                else -> {
                    restoreFragment.stopRefreshAnim()
                    Toast.makeText(context, BackupCodeRender.render(code), Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.deleteEvent.observe(this) { success ->
            if (!success) {
                Toast.makeText(context, R.string.unable_delete_file, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.files.observe(this) {
            adapter.clear()
            adapter.addAll(it)
            adapter.notifyDataSetChanged()
        }

        listView!!.adapter = adapter
        if (AppPermissions.isGranted(context!!, ReadExternalStorage)) {
            viewModel.loadFiles()
        } else {
            AppPermissions.request(this, arrayOf(ReadExternalStorage, WriteExternalStorage), requestList)
        }
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
                is Granted -> viewModel.loadFiles()
                is Denied -> Toast.makeText(context, "Permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (requestCode == requestUpload) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = result!!.data!!
                AppLog.d("Uri: $uri")
                viewModel.backup(uri)
            }
        } else if (requestCode == requestDownload) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = result!!.data
                AppLog.d("Uri: " + uri!!.toString())
                viewModel.backup(uri)
            }
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
            private val restoreListener: View.OnClickListener,
            private val deleteListener: View.OnClickListener,
            private val exportListener: View.OnClickListener)
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
            holder.title.tag = entry.toUri()
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

    private fun createBackupNameDialog(currentName: String): AlertDialog {
        // This example shows how to add a custom layout to an AlertDialog
        return DialogCustom(context!!, 0, R.string.save_widget, R.layout.backup_dialog_enter_name) {
            view, builder ->

            val backupName = view.findViewById<EditText>(R.id.backup_name)
            backupName.setText(currentName)

            builder.setPositiveButton(R.string.backup_save) { _, _ ->
                val filename = backupName.text.toString()
                if (filename.isNotBlank()) {
                    viewModel.backup(filename)
                }
            }

            builder.setNegativeButton(android.R.string.cancel) { _, _ -> }

        }.create()
    }

    private fun download() {
        startActivityForResultSafetly(Intent().forOpenBackup(), requestDownload)
    }

    private fun upload(filename: String, source: File) {
        val uri = FileProvider.getUriForFile(context!!.applicationContext, AUTHORITY, source)
        startActivityForResultSafetly(Intent().forCreateBackup(filename, uri), requestUpload)
    }

    companion object {
        const val requestUpload = 123
        const val requestDownload = 124
        const val requestBackup = 125
        const val requestList = 126

        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider"

        fun create(appWidgetId: Int): FragmentRestoreWidget {
            val fragment = FragmentRestoreWidget()
            val args = Bundle()
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            fragment.arguments = args
            return fragment
        }
    }
}