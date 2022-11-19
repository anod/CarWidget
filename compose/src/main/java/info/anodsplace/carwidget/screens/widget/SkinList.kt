package info.anodsplace.carwidget.screens.widget

import android.content.Context
import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.content.preferences.WidgetInterface

data class SkinList(
        val values: List<String>,
        val titles: List<String>,
        val selectedSkinPosition: Int
) {
    data class Item(val title: String, val value: String)

    val count = values.size
    val current: Item
        get() = Item(titles[selectedSkinPosition], values[selectedSkinPosition])

    constructor(skin: String, context: Context) : this(
            values = WidgetInterface.skins,
            titles = context.resources.getStringArray(R.array.skin_titles).toList(),
            selectedSkinPosition = WidgetInterface.skins.indexOf(skin)
    )

    operator fun get(position: Int): Item = Item(titles[position], values[position])
}
