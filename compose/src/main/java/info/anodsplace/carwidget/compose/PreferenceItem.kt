package info.anodsplace.carwidget.compose

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

sealed class PreferenceItem(
    @StringRes val titleRes: Int,
    val title: String,
    @StringRes val summaryRes: Int,
    val summary: String,
    val key: String
) {
    class Category(@StringRes titleRes: Int = 0, title: String = ""): PreferenceItem(titleRes, title, 0, "", "")
    class Text(@StringRes titleRes: Int = 0, title: String = "", @StringRes summaryRes: Int = 0, summary: String = "", key: String = ""):
        PreferenceItem(titleRes, title, summaryRes, summary, key)
    class Switch(var checked: Boolean, @StringRes titleRes: Int = 0, title: String = "", @StringRes summaryRes: Int = 0, summary: String = "", key: String = ""):
        PreferenceItem(titleRes, title, summaryRes, summary, key)
    class CheckBox(var checked: Boolean, @StringRes titleRes: Int = 0, title: String = "", @StringRes summaryRes: Int = 0, summary: String = "", key: String = ""):
        PreferenceItem(titleRes, title, summaryRes, summary, key)
    class List(
        @StringRes titleRes: Int,
        @StringRes summaryRes: Int = 0,
        key: String = "",
        var value: String = "",
        @ArrayRes val entries: Int,
        @ArrayRes val entryValues: Int
    ): PreferenceItem(titleRes, "", summaryRes, "", key)
}
