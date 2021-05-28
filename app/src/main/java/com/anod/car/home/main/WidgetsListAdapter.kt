package com.anod.car.home.main

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.anod.car.home.R
import com.anod.car.home.utils.ShortcutIconRequestHandler
import com.squareup.picasso.Picasso
import info.anodsplace.carwidget.content.db.LauncherSettings
import info.anodsplace.carwidget.content.db.Shortcut
import info.anodsplace.carwidget.screens.WidgetItem

/**
 * @author alex
 * @date 5/27/13
 */

class WidgetsListAdapter(private val context: Context, private val clickHandler: OnItemClickListener) : androidx.recyclerview.widget.RecyclerView.Adapter<WidgetsListAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: WidgetItem)
    }

    internal var items = listOf<WidgetItem>()

    fun setResult(newItems: List<WidgetItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val resource = when (viewType) {
            1 -> R.layout.list_item_widget_shortcut
            else -> R.layout.list_item_widget_large
        }
        val itemView = LayoutInflater.from(context).inflate(resource, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        if (item is WidgetItem.Large) {
            val shortcuts = item.shortcuts
            val size = shortcuts.size()

            holder.itemView.setOnClickListener {
                clickHandler.onItemClick(items[position])
            }

            sIds.map { holder.itemView.findViewById<ImageView>(it) }.forEachIndexed { i, icon ->
                var info: Shortcut? = null
                if (i < size) {
                    info = shortcuts.get(i)
                }
                if (info != null) {
                    icon.visibility = View.VISIBLE
                    val uri = LauncherSettings.Favorites.getContentUri(context.packageName, info.id).buildUpon()
                            .appendQueryParameter("adaptiveIconStyle", item.adaptiveIconStyle)
                            .build()
                    picasso.load(uri)
                           .into(icon)
                } else {
                    icon.visibility = View.INVISIBLE
                }
            }
        }
    }

    private val picasso: Picasso = Picasso.Builder(context)
            .addRequestHandler(ShortcutIconRequestHandler(context))
            .build()

    val isEmpty: Boolean
        get() = items.isEmpty()

    interface Item

    internal class LargeItem(
        val appWidgetId: Int,
        val shortcuts: SparseArray<Shortcut?>,
        val adaptiveIconStyle: String
    ) : Item

    internal class HintItem : Item

    internal class ShortcutItem : Item

    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is WidgetItem.Shortcut -> 1
            else -> 0
        }
    }

    companion object {
        private val sIds = intArrayOf(
                R.id.imageView0,
                R.id.imageView1,
                R.id.imageView2,
                R.id.imageView3,
                R.id.imageView4,
                R.id.imageView5,
                R.id.imageView6,
                R.id.imageView7)
    }

}