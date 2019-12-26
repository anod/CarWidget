/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.colorpicker

import com.android.colorpicker.ColorPickerSwatch.OnColorSelectedListener
import com.anod.car.home.R

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import androidx.core.os.bundleOf

/**
 * A dialog which takes in as input an array of colors and creates a palette allowing the user to
 * select a specific color swatch, which invokes a listener.
 */
open class ColorPickerDialog : DialogFragment(), OnColorSelectedListener {
    private var palette: ColorPickerPalette? = null
    var listener: OnColorSelectedListener? = null
    var selectedColor: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity

        val size = arguments!!.getInt(KEY_SIZE)
        val columns = arguments!!.getInt(KEY_COLUMNS)
        val colors = arguments!!.getIntArray(KEY_COLORS)
        selectedColor = arguments!!.getInt(KEY_SELECTED_COLOR)
        val tileResId = arguments!!.getInt(KEY_TITLE_ID, R.string.color_picker_default_title)
        val theme = arguments!!.getInt(KEY_THEME)

        val view = LayoutInflater.from(getActivity()).inflate(R.layout.color_picker_dialog, null)
        palette = view.findViewById(R.id.color_picker)
        palette!!.init(size, columns, this)

        palette!!.drawPalette(colors, selectedColor)

        return AlertDialog.Builder(activity!!, theme)
                .setTitle(tileResId)
                .setView(view)
                .create()
    }

    override fun onColorSelected(color: Int) {
        if (listener != null) {
            listener!!.onColorSelected(color)
        }

        if (targetFragment is OnColorSelectedListener) {
            val listener = targetFragment as OnColorSelectedListener?
            listener!!.onColorSelected(color)
        }

        if (color != selectedColor) {
            selectedColor = color
            val colors = arguments!!.getIntArray(KEY_COLORS)
            palette!!.drawPalette(colors, selectedColor)
        }

        dismiss()
    }

    companion object {
        const val SIZE_LARGE = 1
        const val SIZE_SMALL = 2

        const val KEY_TITLE_ID = "title_id"
        const val KEY_COLORS = "colors"
        const val KEY_SELECTED_COLOR = "selected_color"
        const val KEY_COLUMNS = "columns"
        const val KEY_SIZE = "size"
        const val KEY_THEME = "theme"

        fun newInstance(titleResId: Int, colors: IntArray, selectedColor: Int, columns: Int, size: Int, theme: Int) =
                ColorPickerDialog().apply {
                    arguments = bundleOf(
                            KEY_TITLE_ID to titleResId,
                            KEY_COLUMNS to columns,
                            KEY_SIZE to size,
                            KEY_THEME to theme,
                            KEY_COLORS to colors,
                            KEY_SELECTED_COLOR to selectedColor
                    )
                }
    }
}