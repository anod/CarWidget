package info.anodsplace.carwidget.preferences

import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.compose.PreferenceItem
import info.anodsplace.carwidget.content.preferences.InCarInterface

fun createCarScreenItems(inCar: InCarInterface): List<PreferenceItem> {
    return listOf(
        PreferenceItem.Switch(checked = inCar.isInCarEnabled, titleRes = R.string.pref_incar_mode_enabled, key = "incar-mode-enabled"),
        PreferenceItem.Category(titleRes = R.string.pref_detection),
        PreferenceItem.Text(titleRes = R.string.pref_blutooth_device_title, summaryRes = R.string.pref_blutooth_device_summary, key = "bt-device-screen"),
        PreferenceItem.CheckBox(checked = inCar.isHeadsetRequired, key = "headset-required", summaryRes = R.string.pref_headset_connected_summary, titleRes = R.string.pref_headset_connected_title),
        PreferenceItem.CheckBox(checked = inCar.isPowerRequired, key = "power-required", summaryRes = R.string.pref_power_connected_summary, titleRes = R.string.pref_power_connected_title),
        PreferenceItem.CheckBox(checked = inCar.isActivityRequired, key = "activity-recognition", summaryRes = R.string.gms_service_missing, titleRes = R.string.activity_recognition),
        PreferenceItem.CheckBox(checked = inCar.isCarDockRequired, key = "car-dock", summaryRes = R.string.car_dock_summary, titleRes = R.string.car_dock),
        PreferenceItem.Category(titleRes = R.string.pref_actions),
        PreferenceItem.Text(key = "screen-timeout-list", summaryRes = R.string.pref_screen_timeout_summary, titleRes = R.string.pref_screen_timeout),
        PreferenceItem.List(
            value = inCar.brightness,
            entries = R.array.brightness_mode_titles,
            entryValues = R.array.brightness_mode_values,
            key = "brightness",
            summaryRes = R.string.pref_brightness_mode_summary,
            titleRes = R.string.pref_brightness_mode
        ),
        PreferenceItem.List(
            value = inCar.screenOrientation.toString(),
            entries = R.array.orientation_titles,
            entryValues = R.array.orientation_values,
            key = "screen-orientation",
            summaryRes = R.string.pref_screen_orientation_summary,
            titleRes = R.string.screen_orientation
        ),
        PreferenceItem.CheckBox(checked = inCar.isEnableBluetooth, key = "bluetooth", titleRes = R.string.turn_on_bluetooth_summary),
        PreferenceItem.CheckBox(checked = inCar.isAutoSpeaker, key = "auto_speaker", summaryRes = R.string.pref_route_to_speaker_summary, titleRes = R.string.pref_route_to_speaker),
        PreferenceItem.List(
            value = inCar.autoAnswer,
            entries = R.array.autoanswer_titles,
            entryValues = R.array.autoanswer_values,
            key = "auto_answer",
            summaryRes = R.string.pref_auto_answer_summary,
            titleRes = R.string.pref_auto_answer
        ),
        PreferenceItem.Text(key = "media-screen", summaryRes = R.string.pref_change_media_volume_summary, titleRes = R.string.pref_change_media_volume),
        PreferenceItem.Text(key = "more-screen", titleRes = R.string.more),
        PreferenceItem.Category(titleRes = R.string.pref_power_contorl_bt),
        PreferenceItem.CheckBox(checked = inCar.isInCarEnabled, key = "power-bt-enable", summaryRes = R.string.pref_power_plugged_bt_on_summary, titleRes = R.string.pref_power_plugged_bt_on_title),
        PreferenceItem.CheckBox(checked = inCar.isInCarEnabled, key = "power-bt-disable", summaryRes = R.string.pref_power_unplugged_bt_off_summary, titleRes = R.string.pref_power_unplugged_bt_off_title),
        PreferenceItem.Category(titleRes = R.string.notification),
        PreferenceItem.Text(key = "notif-shortcuts", summaryRes = R.string.shortcuts_summary, titleRes = R.string.shortcuts),
    )
}

