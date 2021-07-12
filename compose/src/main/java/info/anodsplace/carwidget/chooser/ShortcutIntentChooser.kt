package info.anodsplace.carwidget.chooser

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.compose.runtime.Composable
import info.anodsplace.carwidget.content.graphics.UtilitiesBitmap

class IntentChooserEntry(

) : ChooserEntry {

    internal var icon: Drawable? = null

    internal var packageName: String? = null

    internal var className: String? = null

    private var extras: Bundle? = null

    internal var intent: Intent? = null


    /**
     * Create a list item and fill it with details from the given
     * [ResolveInfo] object.
     */
    internal constructor(context: Context, pm: PackageManager, resolveInfo: ResolveInfo) {

    }

    /**
     * Build the [Intent] described by this item. If this item
     * can't create a valid [android.content.ComponentName], it
     * will return [Intent.ACTION_CREATE_SHORTCUT] filled with the
     * item label.
     */
    internal fun getIntent(baseIntent: Intent?): Intent {
        if (this.intent != null) {
            return this.intent!!
        }
        val intent = if (baseIntent != null) {
            Intent(baseIntent)
        } else {
            Intent(Intent.ACTION_MAIN)
        }
        if (packageName != null && className != null) {
            // Valid package and class, so fill details as normal intent
            intent.setClassName(packageName!!, className!!)
            if (extras != null) {
                intent.putExtras(extras!!)
            }
        } else {
            // No valid package or class, so treat as shortcut with
            // label
            intent.action = Intent.ACTION_CREATE_SHORTCUT
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label)
        }
        return intent
    }
}

@Composable
fun ShortcutIntentChooser() {

}