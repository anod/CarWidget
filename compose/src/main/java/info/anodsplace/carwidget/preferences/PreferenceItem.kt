package info.anodsplace.carwidget.preferences

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

sealed class PreferenceItem(@StringRes val title: Int, @StringRes val summary: Int, val key: String) {
    class Category(@StringRes title: Int): PreferenceItem(title, 0, "")
    class Text(@StringRes title: Int, @StringRes summary: Int = 0, key: String = ""): PreferenceItem(title, summary, key)
    class Switch(@StringRes title: Int, @StringRes summary: Int = 0, key: String = ""): PreferenceItem(title, summary, key)
    class CheckBox(@StringRes title: Int, @StringRes summary: Int = 0, key: String = ""): PreferenceItem(title, summary, key)
    class List(
        @StringRes title: Int,
        @StringRes summary: Int = 0,
        key: String = "",
        val defaultValue: String = "",
        @ArrayRes entries: Int,
        @ArrayRes entryValues: Int
    ): PreferenceItem(title, summary, key)
}
