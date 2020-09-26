package info.anodsplace.framework.app

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import info.anodsplace.framework.AppLog

val Fragment.applicationContext: Context
    get() = requireContext().applicationContext

fun Fragment.startActivityForResultSafely(intent: Intent, requestCode: Int) {
    try {
        startActivityForResult(intent, requestCode)
    } catch (e: Exception) {
        AppLog.e(e)
        Toast.makeText(context, "Cannot launch activity: $intent", Toast.LENGTH_SHORT).show()
    }
}
