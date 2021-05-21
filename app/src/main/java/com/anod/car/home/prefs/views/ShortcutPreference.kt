package com.anod.car.home.prefs.views

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import androidx.core.view.isVisible

import com.anod.car.home.databinding.PrefShortcutBinding
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
        val binding = PrefShortcutBinding.bind(holder.itemView)

        initDragButton(binding.dragButton, holder.itemView)
        if (iconBitmap != null) {
            binding.icon.setImageBitmap(iconBitmap)
            binding.icon.isVisible = true
            binding.addIcon.isVisible = false
        } else if (showAddIcon) {
            binding.icon.isVisible = false
            binding.addIcon.isVisible = true
        }

        if (showEditButton) {
            binding.deleteButton.setOnClickListener(this)
            binding.deleteButton.isVisible = true
            binding.divider.isVisible = true
            binding.editButton.isVisible = true
        } else {
            binding.deleteButton.setOnClickListener(null)
            binding.deleteButton.isVisible = false
            binding.divider.isVisible = false
            binding.editButton.isVisible = false
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
