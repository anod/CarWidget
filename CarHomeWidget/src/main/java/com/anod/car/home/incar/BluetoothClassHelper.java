package com.anod.car.home.incar;

import android.bluetooth.BluetoothClass;

import com.anod.car.home.R;

public class BluetoothClassHelper {
    public static final int PROFILE_HEADSET = 0;
    public static final int PROFILE_A2DP = 1;

    public static int getBtClassString(BluetoothClass btClass) {
    	int majorDeviceClass = btClass.getMajorDeviceClass();
		if (majorDeviceClass == BluetoothClass.Device.Major.COMPUTER) {
			return R.string.bluetooth_device_laptop;
		} else if (majorDeviceClass == BluetoothClass.Device.Major.PHONE) {
			return R.string.bluetooth_device_cellphone;
		}

        if (doesClassMatch(PROFILE_A2DP,btClass)) {
        	return R.string.bluetooth_device_headphones;

        }
        if (doesClassMatch(PROFILE_HEADSET,btClass)) {
            return R.string.bluetooth_device_headset;
        }
        return 0;
    }
    

    private static boolean doesClassMatch(int profile, BluetoothClass btClass) {
        if (profile == PROFILE_A2DP) {
            if (btClass.hasService(BluetoothClass.Service.RENDER)) {
                return true;
            }
            // By the A2DP spec, sinks must indicate the RENDER service.
            // However we found some that do not (Chordette). So lets also
            // match on some other class bits.
            switch (btClass.getDeviceClass()) {
                case BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO:
                case BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES:
                case BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER:
                case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                    return true;
                default:
                    return false;
            }
        } else if (profile == PROFILE_HEADSET) {
            // The render service class is required by the spec for HFP, so is a
            // pretty good signal
            if (btClass.hasService(BluetoothClass.Service.RENDER)) {
                return true;
            }
            // Just in case they forgot the render service class
            switch (btClass.getDeviceClass()) {
                case BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE:
                case BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                case BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

}
