package com.anod.car.home.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

/**
 * @author algavris
 * @date 08/05/2016.
 */

sealed class PermissionResult
class Granted: PermissionResult()
class Denied: PermissionResult()

object AppPermissions {
    const val REQUEST_STORAGE_WRITE = 300
    const val REQUEST_STORAGE_READ = 301
    const val REQUEST_PHONE = 302
    const val REQUEST_READ_CONTACTS = 303

    fun isGranted(context: Context, permissionName: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permissionName) == PackageManager.PERMISSION_GRANTED
    }

    fun request(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    fun request(fragment: Fragment, permissions: Array<String>, requestCode: Int) {
        fragment.requestPermissions(permissions, requestCode)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray, checkPermission: Int, result: (result: PermissionResult) -> Unit) {
        if (requestCode == checkPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                result(Granted())
            } else {
                result(Denied())
            }
        }
    }
}
