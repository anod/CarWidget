package com.anod.car.home.incar

import com.anod.car.home.R

import android.bluetooth.BluetoothClass

object BluetoothClassHelper {
    private const val PROFILE_HEADSET = 0
    private const val PROFILE_A2DP = 1

    fun getBtClassString(btClass: BluetoothClass): Int {
        val majorDeviceClass = btClass.majorDeviceClass
        if (majorDeviceClass == BluetoothClass.Device.Major.COMPUTER) {
            return R.string.bluetooth_device_laptop
        } else if (majorDeviceClass == BluetoothClass.Device.Major.PHONE) {
            return R.string.bluetooth_device_cellphone
        }

        if (doesClassMatch(PROFILE_A2DP, btClass)) {
            return R.string.bluetooth_device_headphones

        }
        return if (doesClassMatch(PROFILE_HEADSET, btClass)) {
            R.string.bluetooth_device_headset
        } else 0
    }

    private fun doesClassMatch(profile: Int, btClass: BluetoothClass): Boolean {
        if (profile == PROFILE_A2DP) {
            if (btClass.hasService(BluetoothClass.Service.RENDER)) {
                return true
            }
            // By the A2DP spec, sinks must indicate the RENDER service.
            // However we found some that do not (Chordette). So lets also
            // match on some other class bits.
            return when (btClass.deviceClass) {
                BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO, BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES, BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER, BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> true
                else -> false
            }
        } else if (profile == PROFILE_HEADSET) {
            // The render service class is required by the spec for HFP, so is a
            // pretty good signal
            if (btClass.hasService(BluetoothClass.Service.RENDER)) {
                return true
            }
            // Just in case they forgot the render service class
            return when (btClass.deviceClass) {
                BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE, BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET, BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> true
                else -> false
            }
        }
        return false
    }

}
