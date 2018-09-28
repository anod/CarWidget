package com.anod.car.home.app

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.anod.car.home.model.AppsList
import com.anod.car.home.utils.UtilitiesBitmap
import java.util.*

class AppsListAdapter(
        context: Context,
        private val resource: Int,
        private val iconLoader: AppIconLoader) : ArrayAdapter<AppsList.Entry>(context, resource, ArrayList()) {

    private val defaultIconDrawable: BitmapDrawable

    init {
        val defaultIcon = UtilitiesBitmap.makeDefaultIcon(context.packageManager)
        defaultIconDrawable = BitmapDrawable(context.resources, defaultIcon)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var itemView = view
        val holder: ViewHolder
        if (itemView != null) {
            holder = itemView.tag as ViewHolder
        } else {
            val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            itemView = inflater.inflate(resource, parent, false)
            holder = ViewHolder(itemView)
            itemView!!.tag = holder
        }

        val entry = getItem(position)

        holder.title.text = entry!!.title

        if (entry.componentName == null) {
            if (entry.iconRes > 0) {
                holder.icon.visibility = View.VISIBLE
                iconLoader.picasso()
                        .load(entry.iconRes)
                        .placeholder(defaultIconDrawable)
                        .into(holder.icon)
            } else {
                holder.icon.visibility = View.INVISIBLE
            }
        } else {
            holder.icon.visibility = View.VISIBLE
            iconLoader.picasso()
                    .load(Uri.fromParts(AppIconLoader.SCHEME, entry.componentName.flattenToShortString(), null))
                    .placeholder(defaultIconDrawable)
                    .into(holder.icon)
        }
        itemView.id = position
        return itemView
    }


    class ViewHolder(itemView: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(android.R.id.text1)
        var icon: ImageView = itemView.findViewById(android.R.id.icon)
    }
}
