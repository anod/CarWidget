package com.anod.car.home.prefs.colorpicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.android.colorpicker.ColorStateDrawable
import com.anod.car.home.R
import com.anod.car.home.utils.ColorUtils

/**
 * @author alex
 * @date 2014-12-17
 */
class HexPanel @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {

    private val mPreview: ImageView

    var isVisible: Boolean = false
        private set

    private val hexEdit: EditText

    private var alphaSupport: Boolean = false

    init {
        orientation = HORIZONTAL

        LayoutInflater.from(context).inflate(R.layout.color_picker_hex_panel, this)

        hexEdit = findViewById<View>(R.id.hex_edit) as EditText
        mPreview = findViewById<View>(R.id.hex_preview) as ImageView
        if (isInEditMode) {
            setPreviewColor(Color.RED)
        }
    }

    fun init(color: Int, alphaSupport: Boolean) {
        this.alphaSupport = alphaSupport
        val filter0 = InputFilter.LengthFilter(if (alphaSupport) 8 else 6)
        val filter1 = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                val ch = source[i]
                return@InputFilter if (Character.isDigit(ch) || ch in 'A'..'F' || ch in 'a'..'f') {
                    null
                } else {
                    ""
                }
            }
            null
        }

        setColor(color)

        hexEdit.filters = arrayOf(filter0, filter1)
        hexEdit.visibility = View.VISIBLE
        hexEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val value = s.toString()
                if (!TextUtils.isEmpty(value)) {
                    val newColor = ColorUtils.fromHex(value, this@HexPanel.alphaSupport, -1)
                    if (newColor != -1) {
                        setPreviewColor(newColor)
                    }
                }
            }
        })

    }

    fun setColor(color: Int) {
        hexEdit.setText(ColorUtils.toHex(color, alphaSupport))
        setPreviewColor(color)
    }

    private fun setPreviewColor(color: Int) {
        val drawable: Drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.color_picker_swatch, null)!!
        mPreview.setImageDrawable(ColorStateDrawable(arrayOf(drawable), color))
    }

    fun hide() {
        visibility = View.GONE
        isVisible = false
    }

    fun show() {
        visibility = View.VISIBLE
        isVisible = true
    }

    fun getColor(defaultColor: Int): Int {
        return ColorUtils.fromHex(hexEdit.text.toString(), alphaSupport, defaultColor)
    }

}
