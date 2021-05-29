package info.anodsplace.carwidget.compose

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

sealed class PreferenceItem{
    abstract val titleRes: Int
    abstract val title: String
    abstract val summaryRes: Int
    abstract val summary: String
    abstract val key: String

    data class Category(
        @StringRes override val titleRes: Int = 0,
        override val title: String = "",
        @StringRes override val summaryRes: Int = 0,
        override val summary: String = "",
        override val key: String = ""): PreferenceItem()
    data class Text(
        @StringRes override val titleRes: Int = 0,
        override val title: String = "",
        @StringRes override val summaryRes: Int = 0,
        override val summary: String = "",
        override val key: String = ""
    ): PreferenceItem()
    data class Switch(
        var checked: Boolean,
        @StringRes override val titleRes: Int = 0,
        override val title: String = "",
        @StringRes override val summaryRes: Int = 0,
        override val summary: String = "",
        override val key: String = ""
    ): PreferenceItem()
    data class CheckBox(
        var checked: Boolean,
        @StringRes override val titleRes: Int = 0,
        override val title: String = "",
        @StringRes override val summaryRes: Int = 0,
        override val summary: String = "",
        override val key: String = ""
    ): PreferenceItem()
    data class List(
        @ArrayRes val entries: Int,
        @ArrayRes val entryValues: Int,
        var value: String = "",
        @StringRes override val titleRes: Int = 0,
        override val title: String = "",
        @StringRes override val summaryRes: Int = 0,
        override val summary: String = "",
        override val key: String = ""
    ): PreferenceItem()
    data class Placeholder(
        @StringRes override val titleRes: Int = 0,
        override val title: String = "",
        @StringRes override val summaryRes: Int = 0,
        override val summary: String = "",
        override val key: String = ""
    ): PreferenceItem()
}
