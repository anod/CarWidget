package com.anod.car.home.incar

import android.app.UiModeManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import com.anod.car.home.BuildConfig
import com.anod.car.home.prefs.model.InCarInterface
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.utils.PowerUtil
import info.anodsplace.framework.AppLog

/**
 * @author alex
 * @date 12/25/13
 */
object ModeDetector {
    private const val FLAG_POWER = 0
    private const val FLAG_HEADSET = 1
    private const val FLAG_BLUETOOTH = 2
    const val FLAG_ACTIVITY = 3
    private const val FLAG_CAR_DOCK = 4

    private val sPrefState = booleanArrayOf(false, false, false, false, false)
    private val sEventState = booleanArrayOf(false, false, false, false, false)

    private val sTitles = arrayOf("Power", "Headset", "Bluetooth", "Activity", "CarDock")

    private var sMode: Boolean = false

    private val sLock = Any()

    val prefState: BooleanArray
        get() = synchronized(sLock) {
            return sPrefState
        }

    fun getEventState(flag: Int): Boolean {
        return sEventState[flag]
    }

    fun onRegister(context: Context) {
        sEventState[FLAG_POWER] = PowerUtil.isConnected(context)
    }

    fun updatePrefState(prefs: InCarInterface) {
        synchronized(sLock) {
            sPrefState[FLAG_POWER] = prefs.isPowerRequired
            sPrefState[FLAG_BLUETOOTH] = prefs.isBluetoothRequired
            sPrefState[FLAG_HEADSET] = prefs.isHeadsetRequired
            sPrefState[FLAG_ACTIVITY] = prefs.isActivityRequired
            sPrefState[FLAG_CAR_DOCK] = prefs.isCarDockRequired
        }
    }

    fun forceState(prefs: InCarInterface, forceMode: Boolean) {
        updatePrefState(prefs)
        if (sPrefState[FLAG_POWER]) {
            sEventState[FLAG_POWER] = forceMode
        }
        if (sPrefState[FLAG_HEADSET]) {
            sEventState[FLAG_HEADSET] = forceMode
        }
        if (sPrefState[FLAG_BLUETOOTH]) {
            sEventState[FLAG_BLUETOOTH] = forceMode
        }
        if (sPrefState[FLAG_ACTIVITY]) {
            sEventState[FLAG_ACTIVITY] = forceMode
        }
        if (sPrefState[FLAG_CAR_DOCK]) {
            sEventState[FLAG_CAR_DOCK] = forceMode
        }
    }

    fun onBroadcastReceive(context: Context, intent: Intent) {
        val prefs = InCarStorage.load(context)
        if (!prefs.isInCarEnabled) {
            return
        }
        if (Intent.ACTION_POWER_DISCONNECTED == intent.action) {
            onPowerDisconnected(prefs, context)
        } else if (Intent.ACTION_POWER_CONNECTED == intent.action) {
            onPowerConnected(prefs, context)
        }

        updatePrefState(prefs)
        updateEventState(prefs, intent)
        if (BuildConfig.DEBUG) {
            for (i in sPrefState.indices) {
                AppLog.d(sTitles[i] + ": pref - " + sPrefState[i] + ", event - " + sEventState[i])
            }
        }

        val newMode = detectNewMode()
        AppLog.d("New mode : " + newMode + " Car Mode:" + ModeService.sInCarMode)
        if (!ModeService.sInCarMode && newMode) {
            val service = ModeService.createStartIntent(context, ModeService.MODE_SWITCH_ON)
            context.startService(service)
        } else if (ModeService.sInCarMode && !newMode) {
            val service = ModeService.createStartIntent(context, ModeService.MODE_SWITCH_OFF)
            context.stopService(service)
        }

        val action = intent.action
        val inCarEnabled = ModeService.sInCarMode && newMode
        if (inCarEnabled && BluetoothAdapter.ACTION_STATE_CHANGED == action) {
            if (prefs.isAdjustVolumeLevel) {
                ModeHandler.adjustVolume(prefs, context)
            }
        }
    }


    private fun updateEventState(prefs: InCarInterface, intent: Intent) {
        val action = intent.action

        if (ModeBroadcastReceiver.ACTION_ACTIVITY_RECOGNITION == action) {
            sEventState[FLAG_ACTIVITY] = ActivityRecognitionHelper
                    .checkCarState(intent, sEventState[FLAG_ACTIVITY])
            return
        }

        if (Intent.ACTION_POWER_CONNECTED == action) {
            sEventState[FLAG_POWER] = true
            return
        }

        if (Intent.ACTION_POWER_DISCONNECTED == action) {
            sEventState[FLAG_POWER] = false
            return
        }

        if (Intent.ACTION_HEADSET_PLUG == action) {
            sEventState[FLAG_HEADSET] = intent.getIntExtra("state", 0) != 0
            return
        }

        if (Intent.ACTION_DOCK_EVENT == action) {
            val state = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, 0)
            if (state == Intent.EXTRA_DOCK_STATE_CAR) {
                sEventState[FLAG_CAR_DOCK] = true
                return
            } else // if it was previously docked
                if (sEventState[FLAG_CAR_DOCK] && state == Intent.EXTRA_DOCK_STATE_UNDOCKED) {
                    sEventState[FLAG_CAR_DOCK] = false
                    return
                }
        }

        if (UiModeManager.ACTION_ENTER_CAR_MODE == action) {
            sEventState[FLAG_CAR_DOCK] = true
            return
        }

        if (UiModeManager.ACTION_EXIT_CAR_MODE == action) {
            sEventState[FLAG_CAR_DOCK] = false
            return
        }

        if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
            val devices = prefs.btDevices
            if (devices.isNotEmpty()) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (devices.containsKey(device.address)) {
                    sEventState[FLAG_BLUETOOTH] = true
                    return
                }
            }
            return
        }
        if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                sEventState[FLAG_BLUETOOTH] = false
            }
            return
        }
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
            val devices = prefs.btDevices
            if (devices.isNotEmpty()) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (devices.containsKey(device.address)) {
                    sEventState[FLAG_BLUETOOTH] = false
                    return
                }
            }
            return
        }

    }

    private fun detectNewMode(): Boolean {
        var newMode = sMode
        for (i in sPrefState.indices) {
            if (sPrefState[i]) {
                newMode = true
                if (!sEventState[i]) {
                    return false
                }
            }
        }
        return newMode
    }

    private fun onPowerConnected(prefs: InCarInterface, context: Context) {
        if (prefs.isEnableBluetoothOnPower && Bluetooth.state != BluetoothAdapter.STATE_ON) {
            Bluetooth.switchOn()
        }
        if (ModeService.sInCarMode) {
            if (prefs.isDisableScreenTimeoutCharging) {
                ModeService.acquireWakeLock(context)
            }
        }
    }

    private fun onPowerDisconnected(prefs: InCarInterface, context: Context) {
        if (prefs.isDisableBluetoothOnPower && Bluetooth.state != BluetoothAdapter.STATE_OFF) {
            Bluetooth.switchOff()
        }
        if (ModeService.sInCarMode) {
            if (prefs.isDisableScreenTimeoutCharging) {
                ModeService.releaseWakeLock(context)
            }
        }
    }

    /**
     * reset activity state flag
     */
    private fun resetActivityState() {
        sEventState[FLAG_ACTIVITY] = false
        ActivityRecognitionService.resetLastResult()
    }

    fun switchOn(prefs: InCarInterface, modeHandler: ModeHandler) {
        sMode = true
        modeHandler.enable(prefs)
    }

    fun switchOff(prefs: InCarInterface, modeHandler: ModeHandler) {
        sMode = false
        resetActivityState()

        modeHandler.disable(prefs)
    }
}
