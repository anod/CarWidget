package com.anod.car.home.prefs

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anod.car.home.R
import com.anod.car.home.prefs.ActivityPicker.PickAdapter.Item
import com.anod.car.home.utils.*
import info.anodsplace.framework.content.startActivityForResultSafely
import info.anodsplace.framework.livedata.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CarWidgetShortcutsPickerViewModel(application: Application) : AndroidViewModel(application) {
    val result = SingleLiveEvent<Intent?>()

    private suspend fun resolveContact(uri: Uri): Intent? = withContext(Dispatchers.IO) {
        return@withContext Intent().resolveDirectCall(uri, getApplication())
    }

    fun loadContact(uri: Uri) {
        viewModelScope.launch {
            val intent = resolveContact(uri)
            result.value = intent
        }
    }
}

class CarWidgetShortcutsPicker : ActivityPicker() {

    private lateinit var readContactsPermission: ActivityResultLauncher<Void>
    private val viewModel: CarWidgetShortcutsPickerViewModel by viewModels()

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
        readContactsPermission = AppPermissions.register(this, ReadContacts) {

        }
        viewModel.result.observe(this, {
            if (it != null) {
                setResult(RESULT_OK, it)
            } else {
                setResult(RESULT_CANCELED)
            }
            finish()
        })
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (position == IDX_DIRECT_CALL) {
            if (AppPermissions.isGranted(this, ReadContacts)) {
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                startActivityForResultSafely(intent, REQUEST_PICK_CONTACT)
            } else {
                readContactsPermission.launch(null)
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
                viewModel.loadContact(uri)
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        // const val IDX_SWITCH_CAR_MODE = 0
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
    }
}
