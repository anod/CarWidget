package com.anod.car.home.prefs.preferences

import android.content.Context

import com.anod.car.home.model.AbstractShortcuts
import com.anod.car.home.model.NotificationShortcutsModel
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.ShortcutIcon
import com.anod.car.home.model.ShortcutInfo
import com.anod.car.home.model.WidgetShortcutsModel
import com.anod.car.home.backup.PreferencesBackupManager
import com.anod.car.home.prefs.model.InCarSettings
import com.anod.car.home.prefs.model.InCarStorage
import com.anod.car.home.prefs.model.PrefsMigrate
import com.anod.car.home.prefs.model.WidgetStorage

import info.anodsplace.framework.AppLog

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.HashMap

class ObjectRestoreManager(private val mContext: Context) {

    private fun readInCarCompat(readObject: Any): InCarBackup {
        return readObject as? InCarBackup ?: InCarBackup(HashMap(), readObject as InCar)
    }

    fun doRestoreMain(inputStream: InputStream, appWidgetId: Int): Int {
        var prefs: ShortcutsMain? = null
        try {
            synchronized(sLock) {
                val `is` = ObjectInputStream(BufferedInputStream(inputStream))
                prefs = `is`.readObject() as ShortcutsMain
                `is`.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return PreferencesBackupManager.ERROR_FILE_READ
        } catch (e: ClassNotFoundException) {
            AppLog.e(e)
            return PreferencesBackupManager.ERROR_DESERIALIZE
        } catch (e: ClassCastException) {
            AppLog.e(e)
            return PreferencesBackupManager.ERROR_DESERIALIZE
        }

        val main = prefs!!.main
        val widget = WidgetStorage.load(mContext, appWidgetId)
        PrefsMigrate.migrateMain(widget, main)
        widget.apply()

        val shortcuts = prefs!!.shortcuts
        // small check
        if (shortcuts.size % 2 == 0) {
            WidgetStorage.saveLaunchComponentNumber(shortcuts.size, mContext, appWidgetId)
        }
        val model = WidgetShortcutsModel.init(mContext, appWidgetId)
        restoreShortcuts(model, shortcuts)

        return PreferencesBackupManager.RESULT_DONE
    }

    private fun restoreShortcuts(model: AbstractShortcuts, shortcuts: HashMap<Int, ShortcutInfo>) {
        for (pos in 0 until model.count) {
            model.drop(pos)
            val info = shortcuts[pos]
            if (info != null) {
                val newInfo = Shortcut(ShortcutInfo.NO_ID.toLong(), info.itemType, info.title, info.isCustomIcon, info.intent)
                val newIcon = ShortcutIcon(ShortcutInfo.NO_ID.toLong(), info.isCustomIcon, info.isUsingFallbackIcon, info.iconResource, info.icon)

                model.save(pos, newInfo, newIcon)
            }
        }
    }

    fun doRestoreInCar(inputStream: InputStream): Int {
        var inCarBackup: InCarBackup? = null
        try {
            synchronized(sLock) {
                val `is` = ObjectInputStream(BufferedInputStream(inputStream))
                inCarBackup = readInCarCompat(`is`.readObject())
                `is`.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return PreferencesBackupManager.ERROR_FILE_READ
        } catch (e: ClassNotFoundException) {
            AppLog.e(e)
            return PreferencesBackupManager.ERROR_DESERIALIZE
        } catch (e: ClassCastException) {
            AppLog.e(e)
            return PreferencesBackupManager.ERROR_DESERIALIZE
        }

        //version 1.42
        if (inCarBackup!!.inCar.autoAnswer.isNullOrEmpty()) {
            inCarBackup!!.inCar.autoAnswer = InCar.AUTOANSWER_DISABLED
        }

        val dest = InCarStorage.load(mContext)
        migrateIncar(dest, inCarBackup!!.inCar)
        dest.apply()

        val model = NotificationShortcutsModel.init(mContext)

        val shortcuts = inCarBackup!!.notificationShortcuts
        restoreShortcuts(model, shortcuts)

        return PreferencesBackupManager.RESULT_DONE
    }

    companion object {

        val FILE_EXT_DAT = ".dat"
        val FILE_INCAR_DAT = "backup_incar.dat"

        internal val sLock = arrayOfNulls<Any>(0)

        internal fun migrateIncar(dest: InCarSettings, source: InCar) {
            dest.autorunApp = source.autorunApp
            dest.isAdjustVolumeLevel = source.isAdjustVolumeLevel
            dest.isActivateCarMode = source.isActivateCarMode
            dest.isActivityRequired = source.isActivityRequired
            dest.autoAnswer = source.autoAnswer
            dest.isAutoSpeaker = source.isAutoSpeaker
            dest.brightness = source.brightness
            dest.btDevices = source.btDevices
            dest.callVolumeLevel = source.callVolumeLevel
            dest.isCarDockRequired = source.isCarDockRequired
            dest.isDisableScreenTimeoutCharging = source.isDisableScreenTimeoutCharging
            dest.isDisableScreenTimeout = source.isDisableScreenTimeout
            dest.isDisableBluetoothOnPower = source.isDisableBluetoothOnPower
            dest.disableWifi = source.disableWifi
            dest.isEnableBluetooth = source.isEnableBluetooth
            dest.isEnableBluetoothOnPower = source.isEnableBluetoothOnPower
            dest.isHeadsetRequired = source.isHeadsetRequired
            dest.isHotspotOn = source.isHotspotOn
            dest.isInCarEnabled = source.isInCarEnabled
            dest.mediaVolumeLevel = source.mediaVolumeLevel
            dest.isPowerRequired = source.isPowerRequired
            dest.isSamsungDrivingMode = source.isSamsungDrivingMode
            dest.screenOrientation = source.screenOrientation
        }
    }
}