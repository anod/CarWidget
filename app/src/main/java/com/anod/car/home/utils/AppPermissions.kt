package com.anod.car.home.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * @author algavris
 * @date 08/05/2016.
 */

sealed class PermissionResult
object Granted : PermissionResult()
object Denied : PermissionResult()

sealed class AppPermission(val value: String)
object CallPhone : AppPermission(Manifest.permission.CALL_PHONE)
object ReadContacts : AppPermission(Manifest.permission.READ_CONTACTS)

@TargetApi(Build.VERSION_CODES.O)
object AnswerPhoneCalls : AppPermission(Manifest.permission.ANSWER_PHONE_CALLS)
object ModifyPhoneState : AppPermission(Manifest.permission.MODIFY_PHONE_STATE)
object CanDrawOverlay : AppPermission(AppPermissions.Permission.CAN_DRAW_OVERLAY)
object WriteSettings : AppPermission(AppPermissions.Permission.WRITE_SETTINGS)

@RequiresApi(Build.VERSION_CODES.Q)
object ActivityRecognition : AppPermission(Manifest.permission.ACTIVITY_RECOGNITION)

object AppPermissions {
    interface Permission {
        companion object {
            const val CAN_DRAW_OVERLAY = "CAN_DRAW_OVERLAY"
            const val WRITE_SETTINGS = "WRITE_SETTINGS"
        }
    }

    fun isGranted(context: Context, permission: AppPermission): Boolean {
        if (permission == CanDrawOverlay) {
            return Settings.canDrawOverlays(context)
        }
        if (permission == WriteSettings) {
            return Settings.System.canWrite(context)
        }
        if (permission == AnswerPhoneCalls) {
            return ContextCompat.checkSelfPermission(context, permission.value) == PackageManager.PERMISSION_GRANTED
        }
        return ContextCompat.checkSelfPermission(context, permission.value) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowMessage(activity: Activity, permission: AppPermission): Boolean {
        if (isGranted(activity, permission)) {
            return false
        }
        if (permission == CanDrawOverlay) {
            return true
        }
        if (permission == WriteSettings) {
            return true
        }
        if (permission == AnswerPhoneCalls) {
            return true
        }

        if (permission == ActivityRecognition) {
            return true
        }
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.value)
    }
    fun request(activity: Activity, permission: AppPermission, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission.value), requestCode)
    }

    fun request(fragment: Fragment, permissions: Array<AppPermission>, requestCode: Int) {
        fragment.requestPermissions(permissions.map { it.value }.toTypedArray(), requestCode)
    }

    fun request(fragment: Fragment, permission: AppPermission, requestCode: Int) {
        fragment.requestPermissions(arrayOf(permission.value), requestCode)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestDrawOverlay(fragment: Fragment, requestCode: Int) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + fragment.requireContext().packageName))
        fragment.startActivityForResult(intent, requestCode)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestDrawOverlay(activity: Activity, requestCode: Int) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.packageName))
        activity.startActivityForResult(intent, requestCode)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestWriteSettings(fragment: Fragment, requestCode: Int) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + fragment.requireContext().packageName))
        fragment.startActivityForResult(intent, requestCode)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestWriteSettings(activity: Activity, requestCode: Int) {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + activity.packageName))
        activity.startActivityForResult(intent, requestCode)
    }

    fun requestAnswerPhoneCalls(fragment: Fragment, requestCode: Int) {
        request(fragment, AnswerPhoneCalls, requestCode)
    }

    fun requestAnswerPhoneCalls(activity: Activity, requestCode: Int) {
        request(activity, AnswerPhoneCalls, requestCode)
    }

    fun checkResult(requestCode: Int, grantResults: IntArray, checkPermission: Int, result: (result: PermissionResult) -> Unit) {
        if (requestCode == checkPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                result(Granted)
            } else {
                result(Denied)
            }
        }
    }
}
