package info.anodsplace.carwidget.screens.incar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.compose.PreferenceItem

private fun autorunItem(context: Context, componentName: ComponentName?, item: PreferenceItem.Pick): PreferenceItem.Pick {
    val entries = context.resources.getStringArray(R.array.autorun_app_titles)
    val entryValues = context.resources.getStringArray(R.array.autorun_app_values)
    return if (componentName == null) {
        item.copy(
            entries = entries,
            entryValues = entryValues,
            value = "disabled"
        )
    } else {
        val resolveInfo = context.packageManager.resolveActivity(Intent(Intent.ACTION_MAIN).apply {
            component = componentName
        }, 0)
        val appTitle = if (resolveInfo != null) {
            resolveInfo.activityInfo.loadLabel(context.packageManager) as String
        } else componentName.packageName

        item.copy(
            entries = entries.toMutableList().apply {
                add(appTitle)
            }.toTypedArray(),
            entryValues = entryValues.toMutableList().apply {
                add(appTitle)
            }.toTypedArray(),
            value = appTitle
        )
    }
}

fun createCarScreenItems(inCar: InCarInterface, context: Context): List<PreferenceItem> {
    return listOf(
        PreferenceItem.Switch(checked = inCar.isInCarEnabled, titleRes = R.string.pref_incar_mode_enabled, key = "incar-mode-enabled"),
        PreferenceItem.Category(titleRes = R.string.pref_detection),
        PreferenceItem.Text(titleRes = R.string.pref_blutooth_device_title, summaryRes = R.string.pref_blutooth_device_summary, key = "bt-device-screen"),
        PreferenceItem.CheckBox(checked = inCar.isHeadsetRequired, key = "headset-required", summaryRes = R.string.pref_headset_connected_summary, titleRes = R.string.pref_headset_connected_title),
        PreferenceItem.CheckBox(checked = inCar.isPowerRequired, key = "power-required", summaryRes = R.string.pref_power_connected_summary, titleRes = R.string.pref_power_connected_title),
        PreferenceItem.CheckBox(checked = inCar.isActivityRequired, key = "activity-recognition", summaryRes = R.string.gms_service_missing, titleRes = R.string.activity_recognition),
        PreferenceItem.CheckBox(checked = inCar.isCarDockRequired, key = "car-dock", summaryRes = R.string.car_dock_summary, titleRes = R.string.car_dock),
        PreferenceItem.Category(titleRes = R.string.pref_actions),
        PreferenceItem.Switch(
            checked = inCar.isDisableScreenTimeout,
            key = "screen-timeout-list",
            summaryRes = R.string.pref_screen_timeout_summary,
            titleRes = R.string.pref_screen_timeout
        ),
        PreferenceItem.Pick(
            value = inCar.brightness,
            entriesRes = R.array.brightness_mode_titles,
            entryValuesRes = R.array.brightness_mode_values,
            key = "brightness",
            summaryRes = R.string.pref_brightness_mode_summary,
            titleRes = R.string.pref_brightness_mode
        ),
        PreferenceItem.Pick(
            value = inCar.screenOrientation.toString(),
            entriesRes = R.array.orientation_titles,
            entryValuesRes = R.array.orientation_values,
            key = "screen-orientation",
            summaryRes = R.string.pref_screen_orientation_summary,
            titleRes = R.string.screen_orientation
        ),
        PreferenceItem.Switch(checked = inCar.isEnableBluetooth, key = "bluetooth", titleRes = R.string.turn_on_bluetooth_summary),
        PreferenceItem.Switch(checked = inCar.isAutoSpeaker, key = "auto_speaker", summaryRes = R.string.pref_route_to_speaker_summary, titleRes = R.string.pref_route_to_speaker),
        PreferenceItem.Pick(
            value = inCar.autoAnswer,
            entriesRes = R.array.autoanswer_titles,
            entryValuesRes = R.array.autoanswer_values,
            key = "auto_answer",
            summaryRes = R.string.pref_auto_answer_summary,
            titleRes = R.string.pref_auto_answer
        ),
        PreferenceItem.Switch(
            checked = inCar.isAdjustVolumeLevel,
            key = "adjust-volume-level",
            summaryRes = R.string.pref_change_media_volume_summary,
            titleRes = R.string.pref_change_media_volume
        ),
        PreferenceItem.Switch(
            checked = inCar.isActivateCarMode,
            titleRes = R.string.pref_activate_car_mode,
            summaryRes = R.string.pref_activate_car_mode_summary,
            key = "activate-car-mode"),
        autorunItem(context, inCar.autorunApp, item = PreferenceItem.Pick(
            titleRes = R.string.pref_autorun_app_title,
            key = "autorun-app-choose"
        )),
        PreferenceItem.Category(titleRes = R.string.pref_power_contorl_bt),
        PreferenceItem.CheckBox(checked = inCar.isInCarEnabled, key = "power-bt-enable", summaryRes = R.string.pref_power_plugged_bt_on_summary, titleRes = R.string.pref_power_plugged_bt_on_title),
        PreferenceItem.CheckBox(checked = inCar.isInCarEnabled, key = "power-bt-disable", summaryRes = R.string.pref_power_unplugged_bt_off_summary, titleRes = R.string.pref_power_unplugged_bt_off_title),
        PreferenceItem.Category(titleRes = R.string.notification),
        PreferenceItem.Placeholder(key = "notif-shortcuts", summaryRes = R.string.shortcuts_summary, titleRes = R.string.shortcuts)
    )
}