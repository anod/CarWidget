package com.anod.car.home.prefs.model

import android.content.Context
import com.anod.car.home.R

/**
 * @author alex
 * @date 2014-10-20
 */
class SkinList(skin: String, isKeyguard: Boolean, context: Context) {
    class Item(val title: String, val value: String)

    private val items: List<Item>
    val selectedSkinPosition: Int

    init {
        if (isKeyguard) {
            selectedSkinPosition = 0
            items = listOf(Item("Keyguard", "holp"))
        } else {
            val r = context.resources
            val titles = r.getStringArray(R.array.skin_titles)
            val values = r.getStringArray(R.array.skin_values)
            items = titles.mapIndexed { index, title -> Item(title, values[index]) }
            selectedSkinPosition = values.indexOf(skin)
        }
    }

    val count: Int
        get() = items.size

    operator fun get(position: Int): Item {
        return items[position]
    }
}
