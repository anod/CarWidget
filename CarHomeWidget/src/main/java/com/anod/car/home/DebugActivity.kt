package com.anod.car.home

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

import com.anod.car.home.incar.BroadcastService
import com.anod.car.home.incar.ModeDetector
import com.anod.car.home.incar.ModeService
import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.prefs.model.InCarStorage

import info.anodsplace.android.log.AppLog
import com.anod.car.home.utils.LogCatCollector

class DebugActivity : Activity() {

    private val listView: ListView by lazy { findViewById<ListView>(R.id.log) }
    private val textViews: List<TextView> by lazy {
        arrayListOf<TextView>(
            findViewById(R.id.broadcast),
            findViewById(R.id.incar),
            findViewById(R.id.power),
            findViewById(R.id.headset),
            findViewById(R.id.bluetooth),
            findViewById(R.id.activity),
            findViewById(R.id.cardock),
            findViewById(R.id.wakelock)
        )
    }

    private val logAdapter: LogAdapter by lazy { LogAdapter(this) }
    private var handler: Handler = Handler()
    private var runnable: Runnable? = null
    private var inCarPrefs: InCarInterface? = null

    private val isBroadcastServiceRunning: Boolean
        get() {
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (BroadcastService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        listView.adapter = logAdapter
    }

    private fun updateStatus() {

        val isBroadcastServiceRunning = isBroadcastServiceRunning
        setStatusText(textViews[0],
                if (isBroadcastServiceRunning) "Broadcast Service: On" else "Broadcast Service: Off",
                Color.WHITE)

        val isInCarEnabled = InCarStorage.load(this).isInCarEnabled
        setStatusText(textViews[1], if (isInCarEnabled) "InCar: Enabled" else "InCar: Disabled",
                Color.WHITE)

        val powerEvent = ModeDetector.getEventState(ModeDetector.FLAG_POWER.toInt())
        val powerPref = inCarPrefs!!.isPowerRequired
        setStatusText(textViews[2], String.format("Power: %b", powerEvent),
                getColor(powerPref, powerEvent))

        val headsetEvent = ModeDetector.getEventState(ModeDetector.FLAG_HEADSET.toInt())
        val headsetPref = inCarPrefs!!.isHeadsetRequired
        setStatusText(textViews[3], String.format("Headset: %b", headsetEvent),
                getColor(headsetPref, headsetEvent))

        val btEvent = ModeDetector.getEventState(ModeDetector.FLAG_BLUETOOTH.toInt())
        val btPref = inCarPrefs!!.isBluetoothRequired

        var devices = ""
        if (inCarPrefs!!.btDevices != null) {
            devices = TextUtils.join(",", inCarPrefs!!.btDevices.values)
        }

        setStatusText(textViews[4], String.format("Bluetooth: %b [%s]", btEvent, devices),
                getColor(btPref, btEvent))

        val activityEvent = ModeDetector.getEventState(ModeDetector.FLAG_ACTIVITY.toInt())
        val activityPref = inCarPrefs!!.isActivityRequired
        setStatusText(textViews[5], String.format("Activity: %b", activityEvent),
                getColor(activityPref, activityEvent))

        val dockEvent = ModeDetector.getEventState(ModeDetector.FLAG_CAR_DOCK.toInt())
        val docPref = inCarPrefs!!.isCarDockRequired
        setStatusText(textViews[6], String.format("CarDock: %b", dockEvent),
                getColor(docPref, dockEvent))

        val wlHeld = ModeService.isWakeLockHeld(this)
        setStatusText(textViews[7], String.format("WakeLock Held: %b", wlHeld), Color.WHITE)

    }

    private fun getColor(pref: Boolean, event: Boolean): Int {
        return if (pref) if (event) Color.GREEN else Color.RED else Color.GRAY
    }

    private fun setStatusText(textView: TextView, text: String, color: Int) {
        textView.text = text
        textView.setTextColor(color)
    }

    override fun onResume() {
        super.onResume()

        inCarPrefs = InCarStorage.load(this)
        registerLogListener()
        updateStatus()
        AppLog.d("Debug activity resumed")
    }

    override fun onPause() {
        unregisterLogListener()
        super.onPause()
    }


    private fun registerLogListener() {
        runnable = Runnable {
            val out = LogCatCollector.collectLogCat("main")

            logAdapter.clear()
            logAdapter.addAll(out)
            logAdapter.notifyDataSetChanged()

            updateStatus()

            handler.postDelayed(runnable, 1000L)
        }

        handler.postDelayed(runnable, 1000L)

    }

    private fun unregisterLogListener() {
        handler.removeCallbacks(runnable)
    }


    class LogAdapter(context: Context) : ArrayAdapter<String>(context, R.layout.logrow) {

        override fun getItem(position: Int): String? {
            return super.getItem(super.getCount() - position - 1)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                val inflater = (context as Activity).layoutInflater
                view = inflater.inflate(R.layout.logrow, parent, false)
            }

            val entry = getItem(position)
            val text = view!!.findViewById<TextView>(android.R.id.text1)
            text.text = entry

            return view
        }
    }
}
