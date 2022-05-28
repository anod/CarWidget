package info.anodsplace.carwidget.permissions

import androidx.activity.ComponentActivity
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.incar.ScreenOrientation
import info.anodsplace.permissions.AppPermission
import info.anodsplace.permissions.filterRequired

class PermissionChecker(
    private val inCarStatus: InCarStatus,
    private val inCarSettings: InCarInterface
) {
    fun check(activity: ComponentActivity): List<AppPermission> {
        return check(listOf(
            AppPermission.CanDrawOverlay,
            AppPermission.WriteSettings,
            AppPermission.AnswerPhoneCalls,
            AppPermission.ActivityRecognition
        ), activity = activity)
    }

    fun check(list: List<AppPermission>, activity: ComponentActivity): List<AppPermission> {
        if (!inCarStatus.isEnabled) {
            return emptyList()
        }
        return list.filterRequired(activity) {
            return@filterRequired when (it) {
                is AppPermission.CanDrawOverlay -> inCarSettings.screenOrientation != ScreenOrientation.DISABLED
                is AppPermission.WriteSettings -> inCarSettings.brightness != InCarInterface.BRIGHTNESS_DISABLED
                is AppPermission.AnswerPhoneCalls -> inCarSettings.autoAnswer != InCarInterface.AUTOANSWER_DISABLED
                is AppPermission.ActivityRecognition -> inCarSettings.isActivityRequired
                else -> throw IllegalStateException("Unknown $it")
            }
        }
    }
}