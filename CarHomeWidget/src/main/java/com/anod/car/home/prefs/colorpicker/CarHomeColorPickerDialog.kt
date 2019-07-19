package com.anod.car.home.prefs.colorpicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.android.colorpicker.ColorPickerDialog
import com.android.colorpicker.ColorPickerPalette
import com.android.colorpicker.ColorPickerSwatch
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.utils.ColorUtils

class CarHomeColorPickerDialog : ColorPickerDialog() {
    private var palette: ColorPickerPalette? = null
    private var alpha: ColorPickerPalette? = null
    private var selectedAlpha: Int = 0
    private var colorsPanel: View? = null
    private var hexPanel: HexPanel? = null
    private var hexButton: Button? = null
    private var colors: IntArray? = null
    private var alphaSliderVisible: Boolean = false

    private val colorSelectListener = ColorPickerSwatch.OnColorSelectedListener { color ->
        if (color != selectedColor) {
            selectedColor = color
            // Redraw palette to show checkmark on newly selected color before dismissing.
            palette!!.drawPalette(colors, selectedColor)
            if (alpha != null) {
                alpha!!.drawPalette(generateAlphaColors(selectedColor), selectedColor)
            }
            updateHexButton()
        }
    }

    private val alphaSelectListener = ColorPickerSwatch.OnColorSelectedListener { color ->
        val alpha = Color.alpha(color)
        if (alpha != selectedAlpha) {
            selectedAlpha = alpha
            val selectedColorWithAlpha = alphaColor(selectedAlpha, selectedColor)
            // Redraw palette to show checkmark on newly selected color before dismissing.
            this.alpha!!.drawPalette(generateAlphaColors(selectedColor), selectedColorWithAlpha)
            updateHexButton()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alphaSliderVisible = arguments!!.getBoolean(KEY_ALPHA)
        val selectedColor = arguments!!.getInt(KEY_SELECTED_COLOR)
        this.selectedColor = alphaColor(ALPHA_OPAQUE, selectedColor)

        setStyle(STYLE_NO_FRAME, 0)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(activity!!, App.theme(activity!!).dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val size = arguments!!.getInt(KEY_SIZE)
        val columns = arguments!!.getInt(KEY_COLUMNS)
        colors = arguments!!.getIntArray(KEY_COLORS)
        val tileResId = arguments!!.getInt(KEY_TITLE_ID, R.string.color_dialog_title)
        val theme = arguments!!.getInt(KEY_THEME)
        selectedAlpha = Color.alpha(selectedColor)

        context!!.theme.applyStyle(theme, true)
        val view = inflater.inflate(R.layout.color_picker_dialog, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.color_dialog_toolbar)
        toolbar.setTitle(tileResId)

        hexButton = toolbar.findViewById(R.id.hex_switch)
        updateHexButton()
        hexButton!!.setOnClickListener { toggleHexDialog() }

        palette = view.findViewById(R.id.color_picker)
        palette!!.init(size, columns, colorSelectListener)
        colorsPanel = view.findViewById(R.id.colors_panel)

        hexPanel = view.findViewById(R.id.hex_panel)
        hexPanel!!.init(selectedColor, alphaSliderVisible)
        hexPanel!!.hide()

        if (alphaSliderVisible) {
            val density = resources.displayMetrics.density
            alpha = view.findViewById(R.id.alpha_picker)
            alpha!!.background = AlphaPatternDrawable((5 * density).toInt())
            alpha!!.visibility = View.VISIBLE
            alpha!!.init(size, ALPHA_LEVELS, alphaSelectListener)
        }

        val positiveButton = view.findViewById<Button>(android.R.id.button1)
        positiveButton.setText(android.R.string.ok)
        positiveButton.setOnClickListener {
            var color = alphaColor(selectedAlpha, selectedColor)
            if (hexPanel!!.isVisible) {
                color = hexPanel!!.getColor(color)
            }
            listener?.onColorSelected(color)
            dismiss()
        }

        val negativeButton = view.findViewById<Button>(android.R.id.button2)
        negativeButton.setText(android.R.string.cancel)
        negativeButton.setOnClickListener {
            dismiss()
        }

        refreshPalette()
        return view
    }

    private fun updateHexButton() {
        hexButton!!.text = "#${ColorUtils.toHex(selectedColor, alphaSliderVisible)}"
    }

    private fun refreshPalette() {
        if (palette != null) {
            palette!!.drawPalette(colors, selectedColor)
            if (alpha != null) {
                alpha!!.drawPalette(generateAlphaColors(selectedColor), selectedColor)
            }
        }
    }

    private fun alphaColor(alpha: Int, color: Int): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(alpha, r, g, b)
    }

    private fun generateAlphaColors(color: Int): IntArray {
        val colors = IntArray(ALPHA_LEVELS)
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        val inc = ALPHA_OPAQUE / ALPHA_LEVELS
        var alpha = 0
        for (i in 0 until ALPHA_LEVELS - 1) {
            colors[i] = Color.argb(alpha, r, g, b)
            alpha += inc
        }
        colors[ALPHA_LEVELS - 1] = Color.argb(ALPHA_OPAQUE, r, g, b)
        return colors
    }

    private fun toggleHexDialog() {
        if (hexPanel!!.isVisible) {
            hexPanel!!.hide()
            colorsPanel!!.visibility = View.VISIBLE
            return
        }
        hexPanel!!.setColor(selectedColor)
        hexPanel!!.show()
        colorsPanel!!.visibility = View.INVISIBLE
    }

    companion object {
        private const val KEY_ALPHA = "alpha"
        const val ALPHA_LEVELS = 5
        const val ALPHA_OPAQUE = 255

        fun newInstance(selectedColor: Int, alphaSliderVisible: Boolean, context: Context) =
                CarHomeColorPickerDialog().apply {
                    arguments = bundleOf(
                            KEY_COLORS to ColorUtils.colorChoice(context, R.array.color_picker_values),
                            KEY_SELECTED_COLOR to selectedColor,
                            KEY_ALPHA to alphaSliderVisible,
                            KEY_TITLE_ID to R.string.color_dialog_title,
                            KEY_COLUMNS to 5,
                            KEY_SIZE to SIZE_SMALL,
                            KEY_THEME to App.theme(context).dialog
                    )
                }
        }

}
