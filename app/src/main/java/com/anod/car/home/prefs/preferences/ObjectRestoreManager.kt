package com.anod.car.home.prefs.preferences

import android.content.Context
import com.anod.car.home.backup.Backup
import com.anod.car.home.model.*
import com.anod.car.home.prefs.model.*
import info.anodsplace.framework.AppLog
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.*

class ObjectRestoreManager(private val mContext: Context) {

    private fun readInCarCompat(readObject: Any): InCarBackup {
        return readObject as? InCarBackup ?: InCarBackup(HashMap(), readObject as InCar)
    }

    fun doRestoreMain(inputStream: InputStream, appWidgetId: Int): Int {
        var prefs: ShortcutsMain?
        try {
            synchronized(sLock) {
                val `is` = ObjectInputStream(BufferedInputStream(inputStream))
                prefs = `is`.readObject() as ShortcutsMain
                `is`.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return Backup.ERROR_FILE_READ
        } catch (e: ClassNotFoundException) {
            AppLog.e(e)
            return Backup.ERROR_DESERIALIZE
        } catch (e: ClassCastException) {
            AppLog.e(e)
            return Backup.ERROR_DESERIALIZE
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

        return Backup.RESULT_DONE
    }

    private fun restoreShortcuts(model: AbstractShortcuts, shortcuts: HashMap<Int, ShortcutInfo>) {
        for (pos in 0 until model.count) {
            model.drop(pos)
            val info = shortcuts[pos]
            if (info != null) {
                val newInfo = Shortcut(Shortcut.idUnknown, info.itemType, info.title, info.isCustomIcon, info.intent)
                val newIcon = ShortcutIcon(Shortcut.idUnknown, info.isCustomIcon, info.isUsingFallbackIcon, info.iconResource, info.icon)

                model.save(pos, newInfo, newIcon)
            }
        }
    }

    fun doRestoreInCar(inputStream: InputStream): Int {
        var inCarBackup: InCarBackup?
        try {
            synchronized(sLock) {
                val `is` = ObjectInputStream(BufferedInputStream(inputStream))
                inCarBackup = readInCarCompat(`is`.readObject())
                `is`.close()
            }
        } catch (e: IOException) {
            AppLog.e(e)
            return Backup.ERROR_FILE_READ
        } catch (e: ClassNotFoundException) {
            AppLog.e(e)
            return Backup.ERROR_DESERIALIZE
        } catch (e: ClassCastException) {
            AppLog.e(e)
            return Backup.ERROR_DESERIALIZE
        }

        //version 1.42
        if (inCarBackup!!.inCar.autoAnswer.isEmpty()) {
            inCarBackup!!.inCar.autoAnswer = InCarInterface.AUTOANSWER_DISABLED
        }

        val dest = InCarStorage.load(mContext)
        migrateIncar(dest, inCarBackup!!.inCar)
        dest.apply()

        val model = NotificationShortcutsModel.init(mContext)

        val shortcuts = inCarBackup!!.notificationShortcuts
        restoreShortcuts(model, shortcuts)

        return Backup.RESULT_DONE
    }

    companion object {

        const val FILE_EXT_DAT = ".dat"
        const val FILE_INCAR_DAT = "backup_incar.dat"

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
