package info.anodsplace.carwidget.utils

import android.content.Context
import android.content.res.Resources
import androidx.core.content.res.ResourcesCompat
import info.anodsplace.carwidget.content.preferences.WidgetSettings

class DefaultsResourceProvider(private val resources: Resources): WidgetSettings.DefaultsProvider {

    constructor(context: Context): this(context.applicationContext.resources)

    override val tileColor: Int
        get() =  ResourcesCompat.getColor(resources, info.anodsplace.carwidget.skin.R.color.w7_tale_default_background, null)
    override val backgroundColor: Int
        get() = ResourcesCompat.getColor(resources, info.anodsplace.carwidget.skin.R.color.default_background, null)
}