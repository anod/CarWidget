package com.anod.car.home.utils

import android.annotation.SuppressLint
import com.anod.car.home.R
import info.anodsplace.carwidget.content.PermissionDescriptionItem
import info.anodsplace.permissions.AppPermission

@SuppressLint("NewApi")
val permissionDescriptions = listOf(
    PermissionDescriptionItem(
        permission = AppPermission.PostNotification.value,
        iconsRes = R.drawable.baseline_notifications_none_24,
        titleRes = info.anodsplace.carwidget.content.R.string.notifications,
        descRes = info.anodsplace.carwidget.content.R.string.permission_notification_desc
    ),
   PermissionDescriptionItem(
        permission = AppPermission.WriteSettings.value,
        iconsRes = R.drawable.ic_action_brightness_medium,
        titleRes = info.anodsplace.carwidget.content.R.string.permission_write_settings,
        descRes = info.anodsplace.carwidget.content.R.string.adjust_brightness
    ),
    PermissionDescriptionItem(
        permission = AppPermission.CanDrawOverlay.value,
        iconsRes = R.drawable.ic_screen_rotation_black_24dp,
        titleRes = info.anodsplace.carwidget.content.R.string.permission_draw_overlay,
        descRes = info.anodsplace.carwidget.content.R.string.change_screen_orientation
    ),
    PermissionDescriptionItem(
        permission = AppPermission.AnswerPhoneCalls.value,
        iconsRes = R.drawable.ic_action_ring_volume,
        titleRes = info.anodsplace.carwidget.content.R.string.permission_answer_calls,
        descRes = info.anodsplace.carwidget.content.R.string.allow_answer_phone_calls
    ),
    PermissionDescriptionItem(
        permission = AppPermission.PhoneStateRead.value,
        iconsRes = R.drawable.ic_action_ring_volume,
        titleRes = info.anodsplace.carwidget.content.R.string.permission_read_phone_state,
        descRes = info.anodsplace.carwidget.content.R.string.allow_answer_phone_calls
    ),
    PermissionDescriptionItem(
        permission = AppPermission.ActivityRecognition.value,
        iconsRes = R.drawable.ic_action_directions_run_24,
        titleRes = info.anodsplace.carwidget.content.R.string.activity_recognition,
        descRes = info.anodsplace.carwidget.content.R.string.use_gms_for_activity
    ),
)