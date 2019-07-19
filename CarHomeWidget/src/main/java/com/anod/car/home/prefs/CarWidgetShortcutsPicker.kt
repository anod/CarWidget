package com.anod.car.home.prefs

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.core.content.res.ResourcesCompat
import android.view.View
import android.widget.AdapterView

import com.anod.car.home.R
import com.anod.car.home.prefs.ActivityPicker.PickAdapter.Item
import com.anod.car.home.utils.AppPermissions
import com.anod.car.home.utils.ReadContacts
import com.anod.car.home.utils.forDirectCall
import com.anod.car.home.utils.forPickShortcutLocal
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.os.BackgroundTask

import java.util.ArrayList

class CarWidgetShortcutsPicker : ActivityPicker() {

    override val items: List<Item>
        get() {
            val items = ArrayList<Item>()
            val r = resources
            val titles = r.getStringArray(R.array.carwidget_shortcuts)
            for (i in 0 until ITEMS_NUM) {
                val intent = Intent().forPickShortcutLocal(i, titles[i], ICONS[i], this)
                val item = Item(this, titles[i], ResourcesCompat.getDrawable(r, ICONS[i], null),
                        intent)
                items.add(item)
            }
            return items
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.car_widget_shortcuts)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (position == IDX_DIRECT_CALL) {
            if (AppPermissions.isGranted(this, ReadContacts)) {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                startActivityForResult(intent, REQUEST_PICK_CONTACT)
            } else {
                AppPermissions.request(this, ReadContacts, requestReadContacts)
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            return
        }
        super.onItemClick(parent, view, position, id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PICK_CONTACT) {
            val uri = data?.data
            if (resultCode == Activity.RESULT_OK && uri != null) {
                BackgroundTask(object : BackgroundTask.Worker<Uri, Intent?>(this, uri) {

                    override fun run(param: Uri, context: ApplicationContext): Intent? {
                        return Intent().forDirectCall(param, context.actual)
                    }

                    override fun finished(result: Intent?) {
                        if (result != null) {
                            setResult(Activity.RESULT_OK, result)
                        } else {
                            setResult(Activity.RESULT_CANCELED)
                        }
                        finish()
                    }
                }).execute()
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val IDX_SWITCH_CAR_MODE = 0
        const val IDX_DIRECT_CALL = 1
        private const val REQUEST_PICK_CONTACT = 100
        private const val ITEMS_NUM = 5
        private val ICONS = intArrayOf(
                R.drawable.ic_launcher_carwidget,
                R.drawable.ic_shortcut_call,
                R.drawable.ic_shortcut_play,
                R.drawable.ic_shortcut_next,
                R.drawable.ic_shortcut_previous
        )

        const val requestReadContacts = 304
    }
}
