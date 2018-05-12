package com.anod.car.home.drawer

import com.anod.car.home.R
import com.anod.car.home.ui.views.TwoLineButton

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

/**
 * @author alex
 * @date 2014-10-21
 */
class NavigationAdapter(context: Context, private val items: NavigationList) : BaseAdapter() {

    private val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    var selected: Int = 0

    fun onClick(position: Int): Boolean {
        return items.onClick(items[position].id)
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): NavigationList.Item {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var itemView = view
        val holder: ViewHolder
        val item = getItem(position)
        if (itemView == null) {
            val type = getItemViewType(item)
            itemView = layoutInflater.inflate(getItemViewResource(type), parent, false)
            holder = createViewHolder(type, itemView)
            itemView!!.tag = holder
        } else {
            holder = itemView.tag as ViewHolder
        }

        if (item is NavigationList.ActionItem) {
            (holder as ActionViewHolder).setAction(item)
            if (item.id == selected) {
                holder.setSelected(true)
            } else {
                holder.setSelected(false)
            }
        } else {
            (holder as TitleViewHolder).setTitle(item)
        }

        return itemView
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    private fun getItemViewResource(type: Int): Int {
        return if (type == 0) {
            R.layout.navigation_title
        } else R.layout.navigation_action
    }

    private fun createViewHolder(type: Int, view: View?): ViewHolder {
        return if (type == 0) {
            TitleViewHolder(view!!)
        } else ActionViewHolder(view!!)
    }


    override fun getItemViewType(position: Int): Int {
        return getItemViewType(items[position])
    }

    private fun getItemViewType(item: NavigationList.Item): Int {
        return if (item is NavigationList.TitleItem) 0 else 1
    }

    fun refresh() {
        items.refresh()
        notifyDataSetChanged()
    }

    open class ViewHolder

    class TitleViewHolder(view: View) : ViewHolder() {

        internal var title: TextView = view.findViewById(android.R.id.title)

        fun setTitle(item: NavigationList.Item) {
            when {
                item.titleRes > 0 -> {
                    title.setText(item.titleRes)
                    title.visibility = View.VISIBLE
                }
                item.titleText.isNotEmpty() -> {
                    title.text = item.titleText
                    title.visibility = View.VISIBLE
                }
                else -> title.visibility = View.GONE
            }
        }
    }

    class ActionViewHolder(view: View) : ViewHolder() {

        internal var button: TwoLineButton = view.findViewById(R.id.action)

        fun setAction(action: NavigationList.ActionItem) {
            if (action.titleRes > 0) {
                button.setTitle(action.titleRes)
            } else if (action.titleText.isNotEmpty()) {
                button.setTitle(action.titleText)
            }

            when {
                action.summaryRes > 0 -> button.setSummary(action.summaryRes)
                action.summaryText.isNotEmpty() -> button.setSummary(action.summaryText)
                else -> button.setSummaryVisibility(View.GONE)
            }
            button.setIcon(action.iconRes)
        }

        fun setSelected(selected: Boolean) {
            button.isSelected = selected
        }
    }

}
