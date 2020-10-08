package info.anodsplace.carwidget.prefs

import android.os.Bundle
import androidx.compose.runtime.Immutable

interface IntentField {
    val title: String

    interface StringValue: IntentField {
        val value: String?
        fun copy(newValue: String): StringValue
    }

    @Immutable
    data class Action(override val value: String?, override val title: String) : StringValue {
        override fun copy(newValue: String) = copy(value = newValue)
    }
    @Immutable
    data class PackageName(override val value: String?, override val title: String) : StringValue{
        override fun copy(newValue: String) = copy(value = newValue)
    }
    @Immutable
    data class ClassName(override val value: String?, override val title: String) : StringValue{
        override fun copy(newValue: String) = copy(value = newValue)
    }
    @Immutable
    data class Data(override val value: String?, override val title: String) : StringValue{
        override fun copy(newValue: String) = copy(value = newValue)
    }
    @Immutable
    data class MimeType(override val value: String?, override val title: String) : StringValue{
        override fun copy(newValue: String) = copy(value = newValue)
    }
    @Immutable
    data class Categories(override val value: String?, override val title: String) : StringValue{
        override fun copy(newValue: String) = copy(value = newValue)
    }
    @Immutable
    data class Extras(val bundle: Bundle, override val title: String) : IntentField
}