package info.anodsplace.carwidget.permissions

import android.os.Build
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
    private val required = mutableListOf(
        AppPermission.CanDrawOverlay,
        AppPermission.WriteSettings,
        AppPermission.AnswerPhoneCalls,
        AppPermission.ActivityRecognition
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(AppPermission.PostNotification)
        }
    }

    fun check(activity: ComponentActivity): List<AppPermission> {
        return check(list = required, activity = activity)
    }

    @Suppress("NewApi")
    fun check(list: List<AppPermission>, activity: ComponentActivity): List<AppPermission> {
        return list.filterRequired(activity) {
            return@filterRequired when (it) {
                is AppPermission.CanDrawOverlay -> inCarStatus.isEnabled && (
                        inCarSettings.screenOrientation != ScreenOrientation.DISABLED
                        || inCarSettings.screenOnAlert.enabled
                    )
                is AppPermission.WriteSettings -> inCarStatus.isEnabled && inCarSettings.brightness != InCarInterface.BRIGHTNESS_DISABLED
                is AppPermission.AnswerPhoneCalls -> inCarStatus.isEnabled && inCarSettings.autoAnswer != InCarInterface.AUTOANSWER_DISABLED
                is AppPermission.ActivityRecognition -> inCarStatus.isEnabled && inCarSettings.isActivityRequired
                is AppPermission.PostNotification -> true
                else -> throw IllegalStateException("Unknown $it")
            }
        }
    }
}