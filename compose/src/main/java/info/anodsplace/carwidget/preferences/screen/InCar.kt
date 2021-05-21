package info.anodsplace.carwidget.preferences.screen

import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.preferences.PreferenceItem

fun createCarScreenItems(): List<PreferenceItem> {
    return listOf(
        PreferenceItem.Switch(R.string.pref_incar_mode_enabled, key = "incar-mode-enabled"),
        PreferenceItem.Category(R.string.pref_detection),
        PreferenceItem.Text(R.string.pref_blutooth_device_title, summary = R.string.pref_blutooth_device_summary, key = "bt-device-screen"),
        PreferenceItem.Category(title = R.string.pref_detection),
        PreferenceItem.Text(key = "bt-device-screen", summary = R.string.pref_blutooth_device_summary, title = R.string.pref_blutooth_device_title),
        PreferenceItem.CheckBox(key = "headset-required", summary = R.string.pref_headset_connected_summary, title = R.string.pref_headset_connected_title),
        PreferenceItem.CheckBox(key = "power-required", summary = R.string.pref_power_connected_summary, title = R.string.pref_power_connected_title),
        PreferenceItem.CheckBox(key = "activity-recognition", summary = R.string.gms_service_missing, title = R.string.activity_recognition),
        PreferenceItem.CheckBox(key = "car-dock", summary = R.string.car_dock_summary, title = R.string.car_dock),
        PreferenceItem.Category(title = R.string.pref_actions),
        PreferenceItem.Text(key = "screen-timeout-list", summary = R.string.pref_screen_timeout_summary, title = R.string.pref_screen_timeout),
        PreferenceItem.List(
            defaultValue = "disabled",
            entries = R.array.brightness_mode_titles,
            entryValues = R.array.brightness_mode_values,
            key = "brightness",
            summary = R.string.pref_brightness_mode_summary,
            title = R.string.pref_brightness_mode
        ),
        PreferenceItem.List(
            defaultValue = "-1",
            entries = R.array.orientation_titles,
            entryValues = R.array.orientation_values,
            key = "screen-orientation",
            summary = R.string.pref_screen_orientation_summary,
            title = R.string.screen_orientation
        ),
        PreferenceItem.CheckBox(key = "bluetooth", title = R.string.turn_on_bluetooth_summary),
        PreferenceItem.CheckBox(key = "auto_speaker", summary = R.string.pref_route_to_speaker_summary, title = R.string.pref_route_to_speaker),
        PreferenceItem.List(
            defaultValue = "disabled",
            entries = R.array.autoanswer_titles,
            entryValues = R.array.autoanswer_values,
            key = "auto_answer",
            summary = R.string.pref_auto_answer_summary,
            title = R.string.pref_auto_answer
        ),
        PreferenceItem.Text(key = "media-screen", summary = R.string.pref_change_media_volume_summary, title = R.string.pref_change_media_volume),
        PreferenceItem.Text(key = "more-screen", title = R.string.more),
        PreferenceItem.Category(title = R.string.pref_power_contorl_bt),
        PreferenceItem.CheckBox(key = "power-bt-enable", summary = R.string.pref_power_plugged_bt_on_summary, title = R.string.pref_power_plugged_bt_on_title),
        PreferenceItem.CheckBox(key = "power-bt-disable", summary = R.string.pref_power_unplugged_bt_off_summary, title = R.string.pref_power_unplugged_bt_off_title),
        PreferenceItem.Category(title = R.string.notification),
        PreferenceItem.Text(key = "notif-shortcuts", summary = R.string.shortcuts_summary, title = R.string.shortcuts),
    )
}

