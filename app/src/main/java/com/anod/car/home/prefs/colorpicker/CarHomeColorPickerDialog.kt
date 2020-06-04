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
import androidx.core.graphics.alpha
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.android.colorpicker.ColorPickerDialog
import com.android.colorpicker.ColorPickerPalette
import com.android.colorpicker.ColorPickerSwatch
import com.anod.car.home.R
import com.anod.car.home.app.App
import com.anod.car.home.utils.ColorUtils
import com.anod.car.home.utils.opaque
import com.anod.car.home.utils.toColorHex
import com.anod.car.home.utils.withAlpha

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
            // Redraw palette to show check mark on newly selected color before dismissing.
            palette!!.drawPalette(colors, selectedColor)
            val selectedColorWithAlpha = selectedColor.withAlpha(selectedAlpha)
            alpha?.drawPalette(generateAlphaColors(selectedColor), selectedColorWithAlpha)
            updateHexButton()
        }
    }

    private val alphaSelectListener = ColorPickerSwatch.OnColorSelectedListener { color ->
        val alpha = color.alpha
        if (alpha != selectedAlpha) {
            selectedAlpha = alpha
            val selectedColorWithAlpha = selectedColor.withAlpha(selectedAlpha)
            // Redraw palette to show check mark on newly selected color before dismissing.
            this.alpha!!.drawPalette(generateAlphaColors(selectedColor), selectedColorWithAlpha)
            updateHexButton()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alphaSliderVisible = requireArguments().getBoolean(KEY_ALPHA)
        val selectedColor = requireArguments().getInt(KEY_SELECTED_COLOR)
        this.selectedColor = selectedColor.opaque
        selectedAlpha = selectedColor.alpha

        setStyle(STYLE_NO_FRAME, 0)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireActivity(), App.theme(requireActivity()).dialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val size = requireArguments().getInt(KEY_SIZE)
        val columns = requireArguments().getInt(KEY_COLUMNS)
        colors = requireArguments().getIntArray(KEY_COLORS)
        val tileResId = requireArguments().getInt(KEY_TITLE_ID, R.string.color_dialog_title)
        val theme = requireArguments().getInt(KEY_THEME)

        requireContext().theme.applyStyle(theme, true)
        val view = inflater.inflate(R.layout.color_picker_dialog, container, false)

        val toolbar = view.findViewById<Toolbar>(R.id.color_dialog_toolbar)
        toolbar.setTitle(tileResId)

        hexButton = toolbar.findViewById(R.id.hex_switch)
        updateHexButton()
        hexButton!!.setOnClickListener { toggleHexDialog() }

        palette = view.findViewById<ColorPickerPalette>(R.id.color_picker).also {
            it.init(size, columns, colorSelectListener)
        }
        colorsPanel = view.findViewById(R.id.colors_panel)

        hexPanel = view.findViewById<HexPanel>(R.id.hex_panel).also {
            it.init(selectedColor, alphaSliderVisible)
            it.isVisible = false
        }

        if (alphaSliderVisible) {
            val density = resources.displayMetrics.density
            alpha = view.findViewById<ColorPickerPalette>(R.id.alpha_picker).also {
                it.background = AlphaPatternDrawable((5 * density).toInt())
                it.isVisible = true
                it.init(size, ALPHA_LEVELS, alphaSelectListener)
            }
        }

        val positiveButton = view.findViewById<Button>(android.R.id.button1)
        positiveButton.setText(android.R.string.ok)
        positiveButton.setOnClickListener {
            var color = selectedColor.withAlpha(selectedAlpha)
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
        val selectedColorWithAlpha = selectedColor.withAlpha(selectedAlpha)
        hexButton!!.text = "#${selectedColorWithAlpha.toColorHex(alphaSliderVisible)}"
    }

    private fun refreshPalette() {
        palette?.drawPalette(colors, selectedColor)
        val colorWithAlpha = selectedColor.withAlpha(selectedAlpha)
        alpha?.drawPalette(generateAlphaColors(selectedColor), colorWithAlpha)
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
        val panel = hexPanel!!
        if (panel.isVisible) {
            panel.isVisible = false
            colorsPanel!!.isVisible = true
            return
        }
        panel.setColor(selectedColor)
        panel.isVisible = true
        colorsPanel!!.isInvisible = true
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
