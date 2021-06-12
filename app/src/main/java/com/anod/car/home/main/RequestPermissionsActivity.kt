package com.anod.car.home.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anod.car.home.R
import com.anod.car.home.app.CarWidgetActivity
import com.anod.car.home.databinding.ActivityRequestPermissionsBinding
import info.anodsplace.framework.permissions.*

class RequestPermissionsActivity : CarWidgetActivity() {

    private lateinit var binding: ActivityRequestPermissionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val permissions = intent.extras?.getStringArray("permissions") ?: emptyArray()
        if (permissions.isEmpty()) {
            finish()
            return
        }

        binding.listPermissions.adapter = PermissionsAdapter(permissions, this)
        binding.listPermissions.layoutManager = LinearLayoutManager(this)

        binding.buttonAllowAccess.setOnClickListener {
            val p = getPermissions()
            when {
                p.manifestPermissions.isNotEmpty() -> ActivityCompat.requestPermissions(this, p.manifestPermissions, requestPermissionsCode)
                p.canDrawOverlay -> AppPermissions.requestDrawOverlay(this, requestOverlay)
                p.writeSettings -> AppPermissions.requestWriteSettings(this, requestWriteSettings)
                else -> finish()
            }
        }
    }

    class Permissions(
            val canDrawOverlay: Boolean,
            val writeSettings: Boolean,
            val manifestPermissions: Array<String>)

    private fun getPermissions(): Permissions {
        val permissions = intent.extras?.getStringArray("permissions") ?: emptyArray()

        val manifestPermissions = mutableListOf<String>()
        var canDrawOverlay = false
        var writeSettings = false
        permissions.forEach {
            when (it) {
                CanDrawOverlay.value -> canDrawOverlay = true
                WriteSettings.value -> writeSettings = true
                AnswerPhoneCalls.value -> {
                    manifestPermissions.add(it)
                }
                ActivityRecognition.value -> {
                    manifestPermissions.add(it)
                }
                else -> manifestPermissions.add(it)
            }
        }
        return Permissions(canDrawOverlay, writeSettings, manifestPermissions.toTypedArray())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.contains(PackageManager.PERMISSION_DENIED)) {
            setResult(resultPermissionDenied)
        }

        val p = getPermissions()
        when {
            p.canDrawOverlay -> AppPermissions.requestDrawOverlay(this, requestOverlay)
            p.writeSettings -> AppPermissions.requestWriteSettings(this, requestWriteSettings)
            else -> finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val p = getPermissions()
        if (requestCode == requestAnswerPhoneCalls) {
            when {
                p.canDrawOverlay -> AppPermissions.requestDrawOverlay(this, requestOverlay)
                p.writeSettings -> AppPermissions.requestWriteSettings(this, requestWriteSettings)
                else -> finish()
            }
        } else if (requestCode == requestOverlay) {
            if (p.writeSettings) {
                AppPermissions.requestWriteSettings(this, requestWriteSettings)
            } else {
                finish()
            }
        } else if (requestCode == requestWriteSettings) {
            finish()
        } else {
            finish()
        }
    }

    class PermissionsAdapter(var permissions: Array<String>, val context: Context) : RecyclerView.Adapter<PermissionsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
        class Item(@DrawableRes val icon: Int, @StringRes val title: Int, @StringRes val description: Int)

        val settings: info.anodsplace.carwidget.content.preferences.InCarSettings by lazy { info.anodsplace.carwidget.content.preferences.InCarStorage.load(context) }
        val items: List<Item> = permissions.map {
            when (it) {
                WriteSettings.value -> {
                        Item(
                                R.drawable.ic_action_brightness_medium,
                                R.string.permission_write_settings,
                                R.string.adjust_brightness)
                }
                CanDrawOverlay.value -> Item(
                        R.drawable.ic_screen_rotation_black_24dp,
                        R.string.permission_draw_overlay,
                        R.string.change_screen_orientation)
                AnswerPhoneCalls.value -> Item(
                        R.drawable.ic_action_ring_volume,
                        R.string.permission_answer_calls,
                        R.string.allow_answer_phone_calls)
                ActivityRecognition.value -> Item(
                        R.drawable.ic_action_directions_run_24,
                        R.string.activity_recognition,
                        R.string.use_gms_for_activity
                )
                else -> {
                    Item(0, 0, 0)
                }
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.list_item_permission, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val title =holder.itemView.findViewById<TextView>(R.id.title)
            title.setText(item.title)
            val subtitle =holder.itemView.findViewById<TextView>(R.id.subtitle)
            subtitle.setText(item.description)
            val icon =holder.itemView.findViewById<ImageView>(R.id.icon)
            icon.setImageResource(item.icon)
        }
    }

    companion object {
        const val requestPermissionsCode = 1
        const val requestOverlay = 2
        const val requestWriteSettings = 3
        const val requestAnswerPhoneCalls = 4

        const val resultPermissionDenied = 10

        fun start(activity: Activity, permissions: Array<String>, requestCode: Int) {
            val intent = Intent(activity, RequestPermissionsActivity::class.java)
            intent.putExtra("permissions", permissions)
            activity.startActivityForResult(intent, requestCode)
        }
    }
}
