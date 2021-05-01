package com.anod.car.home.prefs.views

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView

import com.anod.car.home.R
import com.anod.car.home.prefs.views.drag.ShortcutDragListener
import com.anod.car.home.prefs.views.drag.ShortcutShadowBuilder

class ShortcutPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs), OnClickListener {

    var appTheme: Int = 0
    var iconBitmap: Bitmap? = null

    var deleteClickListener: OnPreferenceClickListener? = null

    var showEditButton: Boolean = false
    var shortcutPosition: Int = 0

    var dropCallback: DropCallback? = null
    var showAddIcon = false
        set(value) {
            field = value
            if (value) iconBitmap = null
        }

    interface DropCallback {
        fun onScrollRequest(top: Int): Int
        fun onDrop(oldCellId: Int, newCellId: Int): Boolean
    }

    fun requestLayout() {
        super.notifyChanged()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.tag = shortcutPosition

        val dragButton = holder.itemView.findViewById<ImageView>(R.id.drag_button)
        initDragButton(dragButton, holder.itemView)

        val imageView = holder.itemView.findViewById<ImageView>(android.R.id.icon)
        val addIcon = holder.itemView.findViewById<ImageView>(R.id.add_icon)
        if (iconBitmap != null) {
            imageView.setImageBitmap(iconBitmap)
            imageView.visibility = View.VISIBLE
            addIcon.visibility = View.GONE
        } else if (showAddIcon) {
            imageView.visibility = View.GONE
            addIcon.visibility = View.VISIBLE
        }

        val editButton = holder.itemView.findViewById<ImageView>(R.id.delete_button)
        val replaceImage = holder.itemView.findViewById<ImageView>(R.id.edit_button)
        val divider = holder.itemView.findViewById<View>(R.id.divider)
        if (showEditButton) {
            editButton.setOnClickListener(this)
            editButton.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            replaceImage.visibility = View.VISIBLE
        } else {
            editButton.visibility = View.GONE
            divider.visibility = View.GONE
            replaceImage.visibility = View.GONE
        }
    }

    private fun initDragButton(dragButton: ImageView, mainView: View) {
        dragButton.setOnLongClickListener {
            val data = ClipData.newPlainText(shortcutPosition.toString() + "", shortcutPosition.toString() + "")
            mainView.startDragAndDrop(data, ShortcutShadowBuilder(mainView), null, 0)
            true
        }
        dragButton.setOnDragListener(ShortcutDragListener(context, dropCallback))
    }

    override fun onClick(v: View) {
        deleteClickListener?.onPreferenceClick(this)
    }
}
