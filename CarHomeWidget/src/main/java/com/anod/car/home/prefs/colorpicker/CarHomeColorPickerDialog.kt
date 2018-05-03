package com.anod.car.home.prefs.colorpicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar

import com.android.colorpicker.ColorPickerDialog
import com.android.colorpicker.ColorPickerPalette
import com.android.colorpicker.ColorPickerSwatch
import com.anod.car.home.R
import com.anod.car.home.utils.AlphaPatternDrawable
import com.anod.car.home.utils.ColorUtils

class CarHomeColorPickerDialog : ColorPickerDialog() {

    private var alphaSliderVisible1: Boolean = false

    private var palette: ColorPickerPalette? = null

    private var progressBar: ProgressBar? = null

    private var alpha: ColorPickerPalette? = null

    private var selectedAlpha: Int = 0

    private var colorsPanel: View? = null

    private var hexPanel: HexPanel? = null

    private var hexButton: Button? = null

    private val positiveListener = View.OnClickListener {
        var color = selectedColor
        if (hexPanel!!.isVisible) {
            color = hexPanel!!.getColor(color)
        }
        listener?.onColorSelected(color)
        dismiss()
    }

    private val mNegativeListener = View.OnClickListener { dismiss() }

    private val mColorSelectListener = ColorPickerSwatch.OnColorSelectedListener { color ->
        if (color != mSelectedColor) {
            mSelectedColor = color
            // Redraw palette to show checkmark on newly selected color before dismissing.
            palette!!.drawPalette(mColors, mSelectedColor)
            if (alpha != null) {
                alpha!!.drawPalette(generateAlphaColors(mSelectedColor), selectedColor)
            }
            updateHexButton()
        }
    }

    private val mAlphaSelectListener = ColorPickerSwatch.OnColorSelectedListener { color ->
        val alpha = Color.alpha(color)
        if (alpha != selectedAlpha) {
            selectedAlpha = alpha
            // Redraw palette to show checkmark on newly selected color before dismissing.
            this.alpha!!.drawPalette(generateAlphaColors(mSelectedColor), selectedColor)
            updateHexButton()
        }
    }

    fun initialize(colors: IntArray, selectedColor: Int, alphaSliderVisible: Boolean) {
        super.initialize(R.string.color_dialog_title, colors, selectedColor, 5,
                ColorPickerDialog.SIZE_SMALL)
        arguments!!.putBoolean(KEY_ALPHA, alphaSliderVisible)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            alphaSliderVisible1 = arguments!!.getBoolean(KEY_ALPHA)
        }

        if (savedInstanceState != null) {
            alphaSliderVisible1 = savedInstanceState.getBoolean(KEY_ALPHA)
        }

        selectedAlpha = Color.alpha(mSelectedColor)
        mSelectedColor = alphaColor(ALPHA_OPAQUE, mSelectedColor)
        setStyle(DialogFragment.STYLE_NO_FRAME, 0)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(activity!!, R.style.DialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.color_picker_dialog, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.color_dialog_toolbar)
        toolbar.setTitle(R.string.color_dialog_title)

        hexButton = toolbar.findViewById(R.id.hex_switch)
        updateHexButton()
        hexButton!!.setOnClickListener { toggleHexDialog() }

        progressBar = view.findViewById(android.R.id.progress)
        palette = view.findViewById(R.id.color_picker)
        palette!!.init(mSize, mColumns, mColorSelectListener)
        colorsPanel = view.findViewById(R.id.colors_panel)

        hexPanel = view.findViewById(R.id.hex_panel)
        hexPanel!!.init(selectedColor, alphaSliderVisible1)
        hexPanel!!.hide()

        if (alphaSliderVisible1) {
            val density = resources.displayMetrics.density
            alpha = view.findViewById(R.id.alpha_picker)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                alpha!!.background = AlphaPatternDrawable((5 * density).toInt())
            } else {
                alpha!!.setBackgroundDrawable(AlphaPatternDrawable((5 * density).toInt()))
            }
            alpha!!.visibility = View.VISIBLE
            alpha!!.init(mSize, ALPHA_LEVELS, mAlphaSelectListener)
        }

        val positiveButton = view.findViewById<Button>(android.R.id.button1)
        positiveButton.setText(android.R.string.ok)
        positiveButton.setOnClickListener(positiveListener)

        val negativeButton = view.findViewById<Button>(android.R.id.button2)
        negativeButton.setText(android.R.string.cancel)
        negativeButton.setOnClickListener(mNegativeListener)

        showPaletteView()

        return view
    }

    private fun updateHexButton() {
        hexButton!!.text = "#" + ColorUtils.toHex(selectedColor, alphaSliderVisible1)
    }

    override fun showPaletteView() {
        if (progressBar != null && palette != null) {
            progressBar!!.visibility = View.GONE
            refreshPalette()
            palette!!.visibility = View.VISIBLE
        }
    }

    private fun refreshPalette() {
        if (palette != null) {
            palette!!.drawPalette(mColors, mSelectedColor)
            if (alpha != null) {
                alpha!!.drawPalette(generateAlphaColors(mSelectedColor), selectedColor)
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

    override fun getSelectedColor(): Int {
        return alphaColor(selectedAlpha, mSelectedColor)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_ALPHA, alphaSliderVisible1)
    }

    companion object {

        private const val KEY_ALPHA = "alpha"
        const val ALPHA_LEVELS = 5
        const val ALPHA_OPAQUE = 255


        fun newInstance(selectedColor: Int,
                        alphaSliderVisible: Boolean, context: Context): CarHomeColorPickerDialog {
            val ret = CarHomeColorPickerDialog()
            ret.initialize(ColorUtils.colorChoice(context, R.array.color_picker_values), selectedColor,
                    alphaSliderVisible)
            return ret
        }
    }

}
