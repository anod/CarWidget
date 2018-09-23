package com.anod.car.home.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.anod.car.home.app.App

/**
 * @author algavris
 * @date 08/05/2016.
 */

sealed class PermissionResult
object Granted: PermissionResult()
object Denied: PermissionResult()

sealed class AppPermission(val value: String)
object CallPhone: AppPermission(android.Manifest.permission.CALL_PHONE)
object WriteExternalStorage: AppPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
object ReadExternalStorage: AppPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
object ReadContacts: AppPermission(Manifest.permission.READ_CONTACTS)

object AppPermissions {

    fun isGranted(context: Context, permission: AppPermission): Boolean {
        return ContextCompat.checkSelfPermission(context, permission.value) == PackageManager.PERMISSION_GRANTED
    }

    fun request(activity: Activity, permission: AppPermission, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, arrayOf(permission.value), requestCode)
    }

    fun request(activity: Activity, permissions: Array<AppPermission>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions.map { it.value }.toTypedArray(), requestCode)
    }

    fun request(fragment: Fragment, permissions: Array<AppPermission>, requestCode: Int) {
        fragment.requestPermissions(permissions.map { it.value }.toTypedArray(), requestCode)
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
