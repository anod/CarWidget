package info.anodsplace.framework.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.anod.car.home.R
import info.anodsplace.framework.AppLog

fun Activity.startActivityForResultSafely(intent: Intent, requestCode: Int) {
    try {
        this.startActivityForResult(intent, requestCode)
    } catch (e: Exception) {
        AppLog.e(e)
        Toast.makeText(this, "Cannot launch activity: $intent", Toast.LENGTH_SHORT).show()
    }
}

fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.activity_not_found),
                Toast.LENGTH_SHORT).show()
    } catch (e: SecurityException) {
        Toast.makeText(this, getString(R.string.permission_denied),
                Toast.LENGTH_SHORT).show()
        AppLog.e("Widget does not have the permission to launch " + intent +
                ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                "or use the exported attribute for this activity.")
        AppLog.e(e)
    } catch (e: Exception) {
        AppLog.e(e)
        Toast.makeText(this, "Cannot launch activity: $intent", Toast.LENGTH_SHORT).show()
    }
}