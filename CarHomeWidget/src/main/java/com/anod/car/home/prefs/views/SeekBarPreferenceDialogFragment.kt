package com.anod.car.home.prefs.views

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

import com.anod.car.home.R
import info.anodsplace.framework.AppLog


class SeekBarPreferenceDialogFragment : PreferenceDialogFragmentCompat(), SeekBar.OnSeekBarChangeListener {

    private var mSeekBar: SeekBar? = null

    private var valueText: TextView? = null

    private val seekBarDialogPreference: SeekBarDialogPreference
        get() = preference as SeekBarDialogPreference

    fun setValue(value: Int) {
        if (value > seekBarDialogPreference.max) {
            seekBarDialogPreference.value = seekBarDialogPreference.max
        } else {
            seekBarDialogPreference.value = value
        }

        if (mSeekBar != null) {
            mSeekBar!!.progress = seekBarDialogPreference.value
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {

        if (positiveResult) {
            val value = mSeekBar!!.progress
            val preference = seekBarDialogPreference

            if (preference.callChangeListener(value)) {
                setValue(value)
                preference.persistInt(value)
            }
        }
    }

    public override fun onCreateDialogView(context: Context): View {
        val layout = LayoutInflater.from(context).inflate(R.layout.seek_bar_dialog, null) as LinearLayout

        val splashText = layout.findViewById<View>(R.id.splashText) as TextView
        if (seekBarDialogPreference.dialogMessage != null) {
            splashText.text = seekBarDialogPreference.dialogMessage
        } else {
            splashText.text = ""
        }

        val max = seekBarDialogPreference.max
        valueText = layout.findViewById<View>(R.id.value) as EditText

        mSeekBar = layout.findViewById<View>(R.id.seekBar) as SeekBar
        mSeekBar!!.setOnSeekBarChangeListener(this)
        mSeekBar!!.max = max
        mSeekBar!!.progress = seekBarDialogPreference.value

        valueText!!.text = seekBarDialogPreference.value.toString()
        valueText!!.setOnKeyListener { _, _, _ ->
            val value = valueText!!.text.toString()
            var i = -1
            if ("" != value) {
                try {
                    i = Integer.valueOf(value)
                } catch (e: Exception) {
                    AppLog.e(e)
                }

                if (i > max) {
                    i = max
                    valueText!!.text = max.toString()
                }
                if (i != -1) {
                    mSeekBar!!.progress = i
                }
            }
            false
        }

        val suffixView = layout.findViewById<View>(R.id.suffix) as TextView
        suffixView.text = seekBarDialogPreference.suffix

        return layout
    }

    override fun onBindDialogView(v: View) {
        super.onBindDialogView(v)
        mSeekBar!!.max = seekBarDialogPreference.max
        mSeekBar!!.progress = seekBarDialogPreference.value
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val t = progress.toString()
        if (fromUser) {
            valueText!!.text = t
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        // Nothing
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        // Nothing
    }

    companion object {

        fun newInstance(key: String): SeekBarPreferenceDialogFragment {
            val fragment = SeekBarPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString("key", key)
            fragment.arguments = b
            return fragment
        }
    }


}
