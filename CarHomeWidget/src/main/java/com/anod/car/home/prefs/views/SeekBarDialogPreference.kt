package com.anod.car.home.prefs.views

import android.content.Context
import android.content.res.TypedArray
import androidx.preference.DialogPreference
import androidx.preference.PreferenceManager
import android.util.AttributeSet

import com.anod.car.home.R

/**
 * @author alex
 * @date 2015-09-14
 */
class SeekBarDialogPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = R.attr.dialogPreferenceStyle) : DialogPreference(context, attrs, defStyleAttr) {

    val suffix = attrs.getAttributeValue(ANDROIDNS, "text") ?: ""
    val max = attrs.getAttributeIntValue(ANDROIDNS, "max", 100)
    var value: Int = 0

    public override fun persistInt(value: Int): Boolean {
        return super.persistInt(value)
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInt(index, 0)
    }

    override fun onSetInitialValue(restore: Boolean, defaultValue: Any?) {
        value = getPersistedInt(defaultValue as? Int ?: 0)
    }

    companion object {
        private const val ANDROIDNS = "http://schemas.android.com/apk/res/android"
    }
}
