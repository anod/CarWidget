package com.anod.car.home.ui

import com.anod.car.home.R
import com.anod.car.home.model.LauncherSettings
import com.anod.car.home.model.Shortcut
import com.anod.car.home.model.ShortcutIconRequestHandler
import com.squareup.picasso.Picasso

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

/**
 * @author alex
 * @date 5/27/13
 */

internal class WidgetsListAdapter(context: Context) : ArrayAdapter<WidgetsListAdapter.Item>(context, R.layout.widgets_item) {

    private val picasso: Picasso = Picasso.Builder(context)
            .addRequestHandler(ShortcutIconRequestHandler(context))
            .build()

    private val layoutInflater: LayoutInflater = getContext()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private var mCount: Int = 0

    internal interface Item

    internal class LargeItem(var appWidgetId: Int, var shortcuts: SparseArray<Shortcut>) : Item

    internal class ShortcutItem : Item

    internal class HintItem : Item

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {

        return if (mCount == position) {
            getHintView(view)
        } else getWidgetView(position, view, parent)

    }

    private fun getHintView(view: View?): View {
        var hintView = view
        if (hintView == null) {
            val textView = TextView(context)
            textView.setText(R.string.configure_select_item_hint)
            hintView = textView
        }
        return hintView
    }

    private fun getWidgetView(position: Int, view: View?, parent: ViewGroup): View {
        var itemView = view
        val item = getItem(position)

        if (itemView == null) {
            when (item) {
                is ShortcutItem -> itemView = View(parent.context)
                is HintItem -> itemView = layoutInflater.inflate(R.layout.widgets_hint, parent, false)
                else -> itemView = layoutInflater.inflate(R.layout.widgets_item, parent, false)
            }
        }

        if (item is LargeItem) {
            val shortcuts = item.shortcuts
            val size = shortcuts.size()
            for (i in sIds.indices) {
                val icon = itemView!!.findViewById<View>(sIds[i]) as ImageView
                var info: Shortcut? = null
                if (i < size) {
                    info = shortcuts.get(i)
                }

                if (info != null) {
                    icon.visibility = View.VISIBLE
                    picasso.load(LauncherSettings.Favorites.getContentUri(context.packageName, info.id))
                            .into(icon)
                } else {
                    icon.visibility = View.INVISIBLE
                }
            }
        }

        return itemView!!
    }

    override fun getViewTypeCount(): Int {
        return 3
    }

    override fun getItemViewType(position: Int): Int {
        if (mCount == 0) {
            return 0
        }
        return if (1 == position) 1 else 0
    }

    fun setResult(result: WidgetsListLoader.Result?) {
        clear()
        if (result == null) {
            mCount = 0
            return
        }
        mCount = result.large.size()
        if (result.shortcuts.isNotEmpty()) {
            add(ShortcutItem())
            mCount++
        }
        for (i in 0 until result.large.size()) {
            add(LargeItem(result.large.keyAt(i), result.large.valueAt(i)))
        }
        if (result.large.size() > 0) {
            add(HintItem())
        }
    }

    companion object {
        private val sIds = intArrayOf(R.id.imageView0, R.id.imageView1, R.id.imageView2, R.id.imageView3, R.id.imageView4, R.id.imageView5, R.id.imageView6, R.id.imageView7)
    }

}