package info.anodsplace.carwidget.prefs

import android.content.ComponentName
import android.content.Intent
import android.content.pm.ComponentInfo
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Immutable
import androidx.core.os.bundleOf
import info.anodsplace.framework.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

val IntentCategories = mapOf(
        "DEFAULT" to "android.intent.category.DEFAULT",
        "BROWSABLE" to "android.intent.category.BROWSABLE",
        "VOICE" to "android.intent.category.VOICE",
        "ALTERNATIVE" to "android.intent.category.ALTERNATIVE",
        "SELECTED_ALTERNATIVE" to "android.intent.category.SELECTED_ALTERNATIVE",
        "TAB" to "android.intent.category.TAB",
        "LAUNCHER" to "android.intent.category.LAUNCHER",
        "LEANBACK_LAUNCHER" to "android.intent.category.LEANBACK_LAUNCHER",
        "CAR_LAUNCHER" to "android.intent.category.CAR_LAUNCHER",
        "LEANBACK_SETTINGS" to "android.intent.category.LEANBACK_SETTINGS",
        "INFO" to "android.intent.category.INFO",
        "HOME" to "android.intent.category.HOME",
        "HOME_MAIN" to "android.intent.category.HOME_MAIN",
        "SECONDARY_HOME" to "android.intent.category.SECONDARY_HOME",
        "SETUP_WIZARD" to "android.intent.category.SETUP_WIZARD",
        "LAUNCHER_APP" to "android.intent.category.LAUNCHER_APP",
        "PREFERENCE" to "android.intent.category.PREFERENCE",
        "DEVELOPMENT_PREFERENCE" to "android.intent.category.DEVELOPMENT_PREFERENCE",
        "EMBED" to "android.intent.category.EMBED",
        "APP_MARKET" to "android.intent.category.APP_MARKET",
        "MONKEY" to "android.intent.category.MONKEY",
        "UNIT_TEST" to "android.intent.category.UNIT_TEST",
        "SAMPLE_CODE" to "android.intent.category.SAMPLE_CODE",
        "OPENABLE" to "android.intent.category.OPENABLE",
        "TYPED_OPENABLE" to "android.intent.category.TYPED_OPENABLE",
        "FRAMEWORK_INSTRUMENTATION_TEST" to "android.intent.category.FRAMEWORK_INSTRUMENTATION_TEST",
        "CAR_DOCK" to "android.intent.category.CAR_DOCK",
        "DESK_DOCK" to "android.intent.category.DESK_DOCK",
        "LE_DESK_DOCK" to "android.intent.category.LE_DESK_DOCK",
        "HE_DESK_DOCK" to "android.intent.category.HE_DESK_DOCK",
        "CAR_MODE" to "android.intent.category.CAR_MODE",
        "VR_HOME" to "android.intent.category.VR_HOME",
        "APP_BROWSER" to "android.intent.category.APP_BROWSER",
        "APP_CALCULATOR" to "android.intent.category.APP_CALCULATOR",
        "APP_CALENDAR" to "android.intent.category.APP_CALENDAR",
        "APP_CONTACTS" to "android.intent.category.APP_CONTACTS",
        "APP_EMAIL" to "android.intent.category.APP_EMAIL",
        "APP_GALLERY" to "android.intent.category.APP_GALLERY",
        "APP_MAPS" to "android.intent.category.APP_MAPS",
        "APP_MESSAGING" to "android.intent.category.APP_MESSAGING",
        "APP_MUSIC" to "android.intent.category.APP_MUSIC",
        "APP_FILES" to "android.intent.category.APP_FILES",
)
val IntentCategoriesFlip = IntentCategories.keys.associateBy(
        { key -> IntentCategories[key] },
        { key -> key }
)
val IntentFlags = mapOf(
        "ACTIVITY_NO_HISTORY" to 0x40000000,
        "ACTIVITY_SINGLE_TOP" to 0x20000000,
        "ACTIVITY_NEW_TASK" to 0x10000000,
        "ACTIVITY_MULTIPLE_TASK" to 0x08000000,
        "ACTIVITY_CLEAR_TOP" to 0x04000000,
        "ACTIVITY_FORWARD_RESULT" to 0x02000000,
        "ACTIVITY_PREVIOUS_IS_TOP" to 0x01000000,
        "ACTIVITY_EXCLUDE_FROM_RECENTS" to 0x00800000,
        "ACTIVITY_BROUGHT_TO_FRONT" to 0x00400000,
        "ACTIVITY_RESET_TASK_IF_NEEDED" to 0x00200000,
        "ACTIVITY_LAUNCHED_FROM_HISTORY" to 0x00100000,
        "ACTIVITY_NEW_DOCUMENT" to 0x00080000,
        "ACTIVITY_NO_USER_ACTION" to 0x00040000,
        "ACTIVITY_REORDER_TO_FRONT" to 0X00020000,
        "ACTIVITY_NO_ANIMATION" to 0X00010000,
        "ACTIVITY_CLEAR_TASK" to 0X00008000,
        "ACTIVITY_TASK_ON_HOME" to 0X00004000,
        "ACTIVITY_RETAIN_IN_RECENTS" to 0x00002000,
        "ACTIVITY_LAUNCH_ADJACENT" to 0x00001000,
        "ACTIVITY_MATCH_EXTERNAL" to 0x00000800,
)

val Intent.flagNames: List<String>
    get() {
        if (flags == 0) {
            return emptyList()
        }
        val names = mutableListOf<String>()
        for (entry in IntentFlags) {
            if ((flags and entry.value) == entry.value) {
                names.add(entry.key)
            }
        }
        return names
    }

val Intent.categoryNames: List<String>
    get() = categories?.mapNotNull { IntentCategoriesFlip[it] } ?: emptyList()

val packageNameRegex = Regex("(?!^abstract$|^abstract\\..*|.*\\.abstract\\..*|.*\\.abstract$|^assert$|^assert\\..*|.*\\.assert\\..*|.*\\.assert$|^boolean$|^boolean\\..*|.*\\.boolean\\..*|.*\\.boolean$|^break$|^break\\..*|.*\\.break\\..*|.*\\.break$|^byte$|^byte\\..*|.*\\.byte\\..*|.*\\.byte$|^case$|^case\\..*|.*\\.case\\..*|.*\\.case$|^catch$|^catch\\..*|.*\\.catch\\..*|.*\\.catch$|^char$|^char\\..*|.*\\.char\\..*|.*\\.char$|^class$|^class\\..*|.*\\.class\\..*|.*\\.class$|^const$|^const\\..*|.*\\.const\\..*|.*\\.const$|^continue$|^continue\\..*|.*\\.continue\\..*|.*\\.continue$|^default$|^default\\..*|.*\\.default\\..*|.*\\.default$|^do$|^do\\..*|.*\\.do\\..*|.*\\.do$|^double$|^double\\..*|.*\\.double\\..*|.*\\.double$|^else$|^else\\..*|.*\\.else\\..*|.*\\.else$|^enum$|^enum\\..*|.*\\.enum\\..*|.*\\.enum$|^extends$|^extends\\..*|.*\\.extends\\..*|.*\\.extends$|^final$|^final\\..*|.*\\.final\\..*|.*\\.final$|^finally$|^finally\\..*|.*\\.finally\\..*|.*\\.finally$|^float$|^float\\..*|.*\\.float\\..*|.*\\.float$|^for$|^for\\..*|.*\\.for\\..*|.*\\.for$|^goto$|^goto\\..*|.*\\.goto\\..*|.*\\.goto$|^if$|^if\\..*|.*\\.if\\..*|.*\\.if$|^implements$|^implements\\..*|.*\\.implements\\..*|.*\\.implements$|^import$|^import\\..*|.*\\.import\\..*|.*\\.import$|^instanceof$|^instanceof\\..*|.*\\.instanceof\\..*|.*\\.instanceof$|^int$|^int\\..*|.*\\.int\\..*|.*\\.int$|^interface$|^interface\\..*|.*\\.interface\\..*|.*\\.interface$|^long$|^long\\..*|.*\\.long\\..*|.*\\.long$|^native$|^native\\..*|.*\\.native\\..*|.*\\.native$|^new$|^new\\..*|.*\\.new\\..*|.*\\.new$|^package$|^package\\..*|.*\\.package\\..*|.*\\.package$|^private$|^private\\..*|.*\\.private\\..*|.*\\.private$|^protected$|^protected\\..*|.*\\.protected\\..*|.*\\.protected$|^public$|^public\\..*|.*\\.public\\..*|.*\\.public$|^return$|^return\\..*|.*\\.return\\..*|.*\\.return$|^short$|^short\\..*|.*\\.short\\..*|.*\\.short$|^static$|^static\\..*|.*\\.static\\..*|.*\\.static$|^strictfp$|^strictfp\\..*|.*\\.strictfp\\..*|.*\\.strictfp$|^super$|^super\\..*|.*\\.super\\..*|.*\\.super$|^switch$|^switch\\..*|.*\\.switch\\..*|.*\\.switch$|^synchronized$|^synchronized\\..*|.*\\.synchronized\\..*|.*\\.synchronized$|^this$|^this\\..*|.*\\.this\\..*|.*\\.this$|^throw$|^throw\\..*|.*\\.throw\\..*|.*\\.throw$|^throws$|^throws\\..*|.*\\.throws\\..*|.*\\.throws$|^transient$|^transient\\..*|.*\\.transient\\..*|.*\\.transient$|^try$|^try\\..*|.*\\.try\\..*|.*\\.try$|^void$|^void\\..*|.*\\.void\\..*|.*\\.void$|^volatile$|^volatile\\..*|.*\\.volatile\\..*|.*\\.volatile$|^while$|^while\\..*|.*\\.while\\..*|.*\\.while$)(^(?:[a-z_]+(?:\\d*[a-zA-Z_]*)*)(?:\\.[a-z_]+(?:\\d*[a-zA-Z_]*)*)*$)")
val mimeTypeRegex = Regex("\\w+/[-+.\\w]+")

fun flagNamesToInt(flagNames: List<String>): Int {
    var flags = 0
    for (flagNames in flagNames) {
        val flagValue = IntentFlags[flagNames] ?: 0
        flags = flags or flagValue
    }
    return flags
}

interface IntentField {
    val title: String

    interface StringValue: IntentField {
        val value: String?
        fun copy(newValue: String): StringValue
        val isValid: Flow<Boolean>
    }

    @Immutable
    data class None(override val title: String = ""): IntentField

    @Immutable
    data class Action(override val value: String?, override val title: String) : StringValue {
        override fun copy(newValue: String) = copy(value = newValue)
        override val isValid get() = flow { emit(value != null && value.isNotBlank()) }
    }
    @Immutable
    data class PackageName(override val value: String?, override val title: String) : StringValue {
        override fun copy(newValue: String) = copy(value = newValue)
        override val isValid get() = flow {
            val result = withContext(Dispatchers.Default) { value != null && packageNameRegex.matches(value) }
            emit(result)
        }
    }
    @Immutable
    data class ClassName(override val value: String?, override val title: String) : StringValue {
        override fun copy(newValue: String) = copy(value = newValue)
        override val isValid get() = flow {
            val result = withContext(Dispatchers.Default) { value != null && packageNameRegex.matches(value) }
            AppLog.d("ClassName.isValid: $result")
            emit(result)
        }
    }
    @Immutable
    data class Data(override val value: String?, override val title: String) : StringValue {
        override fun copy(newValue: String) = copy(value = newValue)
        override val isValid: Flow<Boolean> get() = flow {
            val result = withContext(Dispatchers.Default) {
                val newValue = value ?: return@withContext false
                return@withContext try {
                    val uri = Uri.parse(newValue)
                    (uri.authority?.isNotBlank() == true
                            || uri.scheme?.isNotBlank() == true
                            || uri.path?.isNotBlank() == true
                            || uri.query?.isNotBlank() == true)
                } catch (e: Exception) {
                    false
                }
            }
            AppLog.d("Data.isValid: $result")
            emit(result)
        }
    }
    @Immutable
    data class MimeType(override val value: String?, override val title: String) : StringValue {
        override fun copy(newValue: String) = copy(value = newValue)
        override val isValid get() = flow {
            val result = withContext(Dispatchers.Default) { value != null && mimeTypeRegex.matches(value) }
            emit(result)
        }
    }
    @Immutable
    data class Categories(val value: Set<String>?, override val title: String) : IntentField
    @Immutable
    data class Flags(val value: Int, override val title: String) : IntentField
    @Immutable
    data class Extras(val bundle: Bundle, override val title: String) : IntentField {
        fun isValid(newValue: Pair<String, Any?>): Flow<Boolean> = flow {
            val result = withContext(Dispatchers.Default) {
                if (newValue.first.isBlank()) {
                    return@withContext false
                }
                return@withContext try {
                    bundleOf(newValue)
                    true
                } catch (e: Exception) {
                    AppLog.e(e)
                    false
                }
            }
            emit(result)
        }
    }

}