package com.anod.car.home.prefs.lookandfeel

import android.app.Application
import android.view.InflateException
import android.view.View
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anod.car.home.R
import com.anod.car.home.appwidget.WidgetViewBuilder
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SkinPreviewViewModel(application: Application): AndroidViewModel(application) {
    val view = MutableLiveData<View>()
    lateinit var builder: WidgetViewBuilder
    var overrideSkin: String? = null

    fun load() {
        viewModelScope.launch {
            val preview = renderPreview(builder, overrideSkin)
            view.value = preview
        }
    }

    private suspend fun renderPreview(builder: WidgetViewBuilder, overrideSkin: String?): View = withContext(Dispatchers.Default) {
        builder.init()
        builder.overrideSkin = overrideSkin
        val rv = builder.build()
        try {
            return@withContext rv.apply(getApplication(), null)
        } catch (e: InflateException) {
            AppLog.e("Cannot generate preview for $overrideSkin", e)
            return@withContext TextView(getApplication()).apply {
                text = context.getString(R.string.cannot_generate_preview)
            }
        }
    }
}