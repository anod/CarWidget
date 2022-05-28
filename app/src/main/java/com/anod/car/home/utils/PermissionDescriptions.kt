package com.anod.car.home.utils

import com.anod.car.home.R
import info.anodsplace.carwidget.content.PermissionDescriptionItem
import info.anodsplace.permissions.AppPermission

val permissionDescriptions = listOf(
   PermissionDescriptionItem(
        permission = AppPermission.WriteSettings.value,
        iconsRes = R.drawable.ic_action_brightness_medium,
        titleRes = R.string.permission_write_settings,
        descRes = R.string.adjust_brightness
    ),
    PermissionDescriptionItem(
        permission = AppPermission.CanDrawOverlay.value,
        iconsRes = R.drawable.ic_screen_rotation_black_24dp,
        titleRes = R.string.permission_draw_overlay,
        descRes = R.string.change_screen_orientation
    ),
    PermissionDescriptionItem(
        permission = AppPermission.AnswerPhoneCalls.value,
        iconsRes = R.drawable.ic_action_ring_volume,
        titleRes = R.string.permission_answer_calls,
        descRes = R.string.allow_answer_phone_calls
    ),
    PermissionDescriptionItem(
        permission = AppPermission.ActivityRecognition.value,
        iconsRes = R.drawable.ic_action_directions_run_24,
        titleRes = R.string.activity_recognition,
        descRes = R.string.use_gms_for_activity
    ),
)