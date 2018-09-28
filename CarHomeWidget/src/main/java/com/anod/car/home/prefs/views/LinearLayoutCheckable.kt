package com.anod.car.home.prefs.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.LinearLayout

class LinearLayoutCheckable : LinearLayout, Checkable {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun isChecked(): Boolean {
        return isSelected
    }

    override fun setChecked(checked: Boolean) {
        isSelected = true
    }

    override fun toggle() {
        isSelected = !isSelected
    }

}
