package com.anod.car.home.prefs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView

import com.anod.car.home.R
import com.anod.car.home.app.AppCompatGridActivity
import info.anodsplace.framework.AppLog
import com.anod.car.home.utils.FastBitmapDrawable
import com.anod.car.home.utils.UtilitiesBitmap

import java.util.ArrayList
import java.util.Collections

open class ActivityPicker : AppCompatGridActivity() {

    /**
     * Adapter of items that are displayed in this dialog.
     */
    private val pickAdapter: PickAdapter by lazy { PickAdapter(this, items) }

    /**
     * Base [Intent] used when building list.
     */
    private var mBaseIntent: Intent? = null

    /**
     * Build and return list of items to be shown in dialog. Default
     * implementation mixes activities matching [.mBaseIntent] from
     * [.putIntentItems] with any injected items from
     * [Intent.EXTRA_SHORTCUT_NAME]. Override this method in subclasses to
     * change the items shown.
     */
    protected open val items: List<PickAdapter.Item>
        get() {
            val packageManager = packageManager
            val items = ArrayList<PickAdapter.Item>()
            val intent = intent
            val labels = intent.getStringArrayListExtra(Intent.EXTRA_SHORTCUT_NAME)
            val icons = intent
                    .getParcelableArrayListExtra<ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)

            if (labels != null && icons != null && labels.size == icons.size) {
                for (i in labels.indices) {
                    val label = labels[i]
                    var icon: Drawable? = null

                    try {
                        val iconResource = icons[i]
                        val res = packageManager
                                .getResourcesForApplication(iconResource.packageName)
                        icon = ResourcesCompat.getDrawable(res, res.getIdentifier(iconResource.resourceName, null, null), null)
                    } catch (e: NameNotFoundException) {
                        AppLog.e(e)
                    }

                    items.add(PickAdapter.Item(this, label, icon))
                }
            }
            if (mBaseIntent != null) {
                putIntentItems(mBaseIntent!!, items)
            }

            return items
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent

        initPicker(intent)
    }

    protected fun initPicker(intent: Intent) {

        // Read base intent from extras
        val parcel = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_INTENT)
        if (parcel is Intent) {
            mBaseIntent = parcel
        }

        val title = intent.getStringExtra(Intent.EXTRA_TITLE)
        if (title != null) {
            setTitle(title)
        }

        // Build list adapter of pickable items
        listAdapter = pickAdapter
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val intent = getIntentForPosition(position)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    /**
     * Build the specific [Intent] for a given list position. Convenience
     * method that calls through to [PickAdapter.Item.getIntent].
     */
    protected fun getIntentForPosition(position: Int): Intent {
        val item = pickAdapter.getItem(position) as PickAdapter.Item
        return item.getIntent(mBaseIntent)
    }


    /**
     * Fill the given list with any activities matching the base [Intent].
     */
    protected fun putIntentItems(baseIntent: Intent, items: MutableList<PickAdapter.Item>) {
        val packageManager = packageManager
        val list = packageManager.queryIntentActivities(
                baseIntent, 0 /* no flags */
        )
        Collections.sort(list, ResolveInfo.DisplayNameComparator(packageManager))

        val listSize = list.size
        for (i in 0 until listSize) {
            val resolveInfo = list[i]
            items.add(PickAdapter.Item(this, packageManager, resolveInfo))
        }
    }

    /**
     * Adapter which shows the set of activities that can be performed for a
     * given [Intent].
     */
    protected class PickAdapter(context: Context, private val mItems: List<Item>) : BaseAdapter() {

        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        /**
         * Item that appears in a [PickAdapter] list.
         */
        class Item {

            var label: CharSequence? = null
                internal set

            internal var icon: Drawable

            internal var packageName: String? = null

            internal var className: String? = null

            internal var extras: Bundle? = null

            internal var intent: Intent? = null

            /**
             * Create a list item from given label and icon.
             */
            internal constructor(context: Context, label: CharSequence, icon: Drawable?, intent: Intent) {
                this.label = label
                this.icon = createThumbnail(icon, context)
                this.intent = intent
            }

            /**
             * Create a list item from given label and icon.
             */
            internal constructor(context: Context, label: CharSequence, icon: Drawable?) {
                this.label = label
                this.icon = createThumbnail(icon, context)
            }

            /**
             * Create a list item and fill it with details from the given
             * [ResolveInfo] object.
             */
            internal constructor(context: Context, pm: PackageManager, resolveInfo: ResolveInfo) {
                label = resolveInfo.loadLabel(pm)
                if (label == null && resolveInfo.activityInfo != null) {
                    label = resolveInfo.activityInfo.name
                }

                icon = createThumbnail(resolveInfo.loadIcon(pm), context)
                packageName = resolveInfo.activityInfo.applicationInfo.packageName
                className = resolveInfo.activityInfo.name
            }

            /**
             *
             * @param source
             * @param context
             * @return
             */
            private fun createThumbnail(source: Drawable?, context: Context): Drawable {
                val bitmap: Bitmap?
                if (source == null) {
                    bitmap = UtilitiesBitmap.makeDefaultIcon(context.packageManager)
                    return FastBitmapDrawable(bitmap)
                }
                bitmap = UtilitiesBitmap.createSystemIconBitmap(source, context)
                return FastBitmapDrawable(bitmap)
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

        override fun getCount(): Int {
            return mItems.size
        }

        override fun getItem(position: Int): Any {
            return mItems[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView: TextView
            if (convertView == null) {
                textView = inflater.inflate(R.layout.pick_item, parent, false) as TextView
            } else {
                textView = convertView as TextView
            }

            val item = getItem(position) as Item
            textView.text = item.label
            textView.setCompoundDrawablesWithIntrinsicBounds(null, item.icon, null, null)

            return textView
        }
    }
}
