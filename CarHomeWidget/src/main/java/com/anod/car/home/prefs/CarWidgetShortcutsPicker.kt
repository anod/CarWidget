package com.anod.car.home.prefs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.widget.AdapterView

import com.anod.car.home.R
import com.anod.car.home.prefs.ActivityPicker.PickAdapter.Item
import com.anod.car.home.utils.AppPermissions
import com.anod.car.home.utils.IntentUtils
import info.anodsplace.framework.app.ApplicationContext
import info.anodsplace.framework.os.BackgroundTask

import java.util.ArrayList

class CarWidgetShortcutsPicker : ActivityPicker() {

    override val items: List<Item>
        get() {
            val items = ArrayList<PickAdapter.Item>()
            val r = resources
            val titles = r.getStringArray(R.array.carwidget_shortcuts)
            for (i in 0 until ITEMS_NUM) {
                val intent = IntentUtils.createPickShortcutLocalIntent(i, titles[i], ICONS[i], this)
                val item = PickAdapter.Item(this, titles[i], ResourcesCompat.getDrawable(r, ICONS[i], null),
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
        if (position == IntentUtils.IDX_DIRECT_CALL) {
            if (AppPermissions.isGranted(this, Manifest.permission.READ_CONTACTS)) {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                startActivityForResult(intent, REQUEST_PICK_CONTACT)
            } else {
                AppPermissions.request(this, arrayOf(Manifest.permission.READ_CONTACTS), requestReadContacts)
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            return
        }
        super.onItemClick(parent, view, position, id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                BackgroundTask(object : BackgroundTask.Worker<Uri, Intent?>(this, data.data) {

                    override fun run(uri: Uri, context: ApplicationContext): Intent? {
                        return IntentUtils.createDirectCallIntent(uri, context.actual)
                    }

                    override fun finished(intent: Intent?) {
                        if (intent != null) {
                            setResult(Activity.RESULT_OK, intent)
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
        private const val REQUEST_PICK_CONTACT = 100
        private const val ITEMS_NUM = 5
        private val ICONS = intArrayOf(
                R.drawable.ic_launcher_carwidget,
                R.drawable.ic_call_white_24dp,
                R.drawable.ic_media_play_pause,
                R.drawable.ic_media_next,
                R.drawable.ic_media_prev)

        const val requestReadContacts = 304
    }
}
