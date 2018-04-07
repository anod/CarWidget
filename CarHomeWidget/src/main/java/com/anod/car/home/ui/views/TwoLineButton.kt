package com.anod.car.home.ui.views

import com.anod.car.home.R

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * @author alex
 * @date 2014-10-20
 */
class TwoLineButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val iconView: ImageView by lazy { findViewById<ImageView>(android.R.id.icon) }

    private val titleView: TextView by lazy { findViewById<TextView>(android.R.id.title) }

    private val summaryView: TextView by lazy { findViewById<TextView>(android.R.id.summary) }

    init {
        orientation = LinearLayout.HORIZONTAL

        LayoutInflater.from(context).inflate(R.layout.two_line_button, this)

        var summary: CharSequence? = null
        val ta = context
                .obtainStyledAttributes(attrs, R.styleable.TwoLineButton, defStyle, 0)
        val n = ta.indexCount
        for (i in 0 until n) {
            val attr = ta.getIndex(i)
            when (attr) {
                R.styleable.TwoLineButton_icon_src -> {
                    val d = ta.getDrawable(attr)
                    if (d != null) {
                        setIcon(d)
                    }
                }
                R.styleable.TwoLineButton_title_text -> setTitle(ta.getText(attr))
                R.styleable.TwoLineButton_summary_text -> summary = ta.getText(attr)
                R.styleable.TwoLineButton_single_line -> {
                    val value = ta.getBoolean(attr, true)
                    titleView.setSingleLine(value)
                    if (value) {
                        titleView.ellipsize = TextUtils.TruncateAt.END
                    } else {
                        titleView.ellipsize = null
                    }
                }
            }
        }
        ta.recycle()

        if (summary == null) {
            summaryView.visibility = View.GONE
        } else {
            summaryView.text = summary
        }

    }


    private fun setIcon(drawable: Drawable) {
        iconView.setImageDrawable(drawable)
    }

    fun setIcon(resId: Int) {
        iconView.setImageResource(resId)
    }

    fun setTitle(text: CharSequence) {
        titleView.text = text
    }

    fun setTitle(resId: Int) {
        titleView.setText(resId)
    }

    fun setSummary(resId: Int) {
        summaryView.setText(resId)
        summaryView.visibility = View.VISIBLE
    }

    fun setSummary(text: CharSequence) {
        summaryView.text = text
        summaryView.visibility = View.VISIBLE
    }

    fun setSummaryVisibility(visibility: Int) {
        summaryView.visibility = visibility
    }
}
