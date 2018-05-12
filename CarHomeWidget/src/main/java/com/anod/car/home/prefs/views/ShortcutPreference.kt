package com.anod.car.home.prefs.views

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceViewHolder
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView

import com.anod.car.home.R
import com.anod.car.home.prefs.views.drag.ShortcutDragListener
import com.anod.car.home.prefs.views.drag.ShortcutShadowBuilder

class ShortcutPreference : Preference, OnClickListener {

    private var appTheme: Int = 0

    private var mIconBitmap: Bitmap? = null

    private var iconResource = INVALID_RESOURCE

    var deleteClickListener: Preference.OnPreferenceClickListener? = null

    private var showEditButton: Boolean = false

    var shortcutPosition: Int = 0

    private var mDropCallback: DropCallback? = null

    interface DropCallback {
        fun onScrollRequest(top: Int): Int
        fun onDrop(oldCellId: Int, newCellId: Int): Boolean
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        layoutResource = R.layout.pref_shortcut
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        layoutResource = R.layout.pref_shortcut
    }

    constructor(context: Context) : super(context) {
        layoutResource = R.layout.pref_shortcut
    }

    fun setDropCallback(dropCallback: DropCallback) {
        mDropCallback = dropCallback
    }

    fun showButtons(show: Boolean) {
        showEditButton = show
        notifyChanged()
    }

    fun setAppTheme(theme: Int) {
        appTheme = theme
    }

    fun setIconResource(resId: Int) {
        mIconBitmap = null
        iconResource = resId
        notifyChanged()
    }

    fun setIconBitmap(iconBitmap: Bitmap) {
        mIconBitmap = iconBitmap
        iconResource = INVALID_RESOURCE
        notifyChanged()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.tag = shortcutPosition

        val dragButton = holder.itemView.findViewById<View>(R.id.drag_button) as ImageView
        initDragButton(dragButton, holder.itemView)

        val imageView = holder.itemView.findViewById<View>(android.R.id.icon) as ImageView
        if (mIconBitmap != null) {
            imageView.setImageBitmap(mIconBitmap)
            imageView.visibility = View.VISIBLE
        }
        if (iconResource > 0) {
            imageView.setImageResource(iconResource)
            imageView.visibility = View.VISIBLE
        }

        val editButton = holder.itemView.findViewById<View>(R.id.delete_button) as ImageView
        val replaceImage = holder.itemView.findViewById<View>(R.id.edit_button) as ImageView
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mainView.startDragAndDrop(data, ShortcutShadowBuilder(mainView), null, 0)
            } else {
                mainView.startDrag(data, ShortcutShadowBuilder(mainView), null, 0)
            }

            true
        }
        dragButton.setOnDragListener(
                ShortcutDragListener(context, appTheme, mDropCallback))
    }

    override fun onClick(v: View) {
        deleteClickListener?.onPreferenceClick(this)
    }

    companion object {
        private const val INVALID_RESOURCE = 0
    }
}
