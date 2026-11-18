package info.anodsplace.carwidget.shortcut.intent

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Immutable
import androidx.core.os.bundleOf
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.shortcuts.ShortcutIntentPolicy
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
        "ACTIVITY_NEW_DOCUMENT" to Intent.FLAG_ACTIVITY_NEW_DOCUMENT,
        "ACTIVITY_NO_USER_ACTION" to 0x00040000,
        "ACTIVITY_REORDER_TO_FRONT" to 0X00020000,
        "ACTIVITY_NO_ANIMATION" to 0X00010000,
        "ACTIVITY_CLEAR_TASK" to 0X00008000,
        "ACTIVITY_TASK_ON_HOME" to 0X00004000,
        "ACTIVITY_RETAIN_IN_RECENTS" to 0x00002000,
        "ACTIVITY_LAUNCH_ADJACENT" to 0x00001000,
        "ACTIVITY_MATCH_EXTERNAL" to 0x00000800,
        "FROM_BACKGROUND" to Intent.FLAG_FROM_BACKGROUND,
)

internal val editableIntentFlags = IntentFlags.filterValues { it in ShortcutIntentPolicy.flags }
internal val editableIntentCategories = IntentCategories.filterValues { it in ShortcutIntentPolicy.categories }

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

internal fun updateIntentFlags(flags: Int, key: String, checked: Boolean): Int {
    val mask = editableIntentFlags.getValue(key)
    return if (checked) flags or mask else flags and mask.inv()
}

internal fun updateIntentCategories(categories: Set<String>?, key: String, checked: Boolean): Set<String> {
    val existing = categories.orEmpty()
    val category = editableIntentCategories.getValue(key)
    return if (checked) existing + category else existing - category
}

interface IntentField {

    enum class Suggestions { None, Action, MimeType, Component }

    interface StringValue: IntentField {
        fun select(intent: Intent): String
        val title: String
        fun copy(value: String?): StringValue
        val value: String?
        val isValid: Boolean
        val suggestions: Suggestions
    }

    @Immutable
    object None: IntentField

    @Immutable
    class Action(override val value: String?, override val title: String) : StringValue {
        override fun select(intent: Intent): String = intent.action ?: ""
        override val suggestions = Suggestions.Action
        override fun copy(value: String?) = Action(value, title)
        override val isValid: Boolean
            get() = value.isNullOrBlank() || value in ShortcutIntentPolicy.actions
    }

    @Immutable
    class Component(val value: ComponentName?) : IntentField {
        val isValid: Boolean
            get() = value == null
                    || (value.packageName.isBlank() && value.className.isBlank())
                    || (packageNameRegex.matches(value.packageName)
                    && componentClassNameRegex.matches(value.className))
    }

    @Immutable
    class Data(val uri: Uri?, override val title: String) : StringValue {
        override fun select(intent: Intent): String = intent.data?.toString() ?: ""
        override val suggestions = Suggestions.None
        override fun copy(value: String?) = Data(if (value == null) null else Uri.parse(value), title)
        override val value: String?
            get() = uri?.toString()
        override val isValid: Boolean
            get() {
                val newValue = uri ?: return true
                if (newValue.toString().isBlank()) {
                    return true
                }
                val result = try {
                    newValue.authority?.isNotBlank() == true
                            || newValue.scheme?.isNotBlank() == true
                            || newValue.path?.isNotBlank() == true
                            || newValue.query?.isNotBlank() == true
                } catch (e: Exception) {
                    false
                }
                AppLog.d("Data.isValid: $result")
                return result
            }
    }

    @Immutable
    class MimeType(override val value: String?, override val title: String) : StringValue {
        override val suggestions = Suggestions.MimeType
        override fun select(intent: Intent): String = intent.type.orEmpty()
        override fun copy(value: String?) = MimeType(value, title)
        override val isValid: Boolean
            get() = value.isNullOrBlank() || value in ShortcutIntentPolicy.mimeTypes
    }

    @Immutable
    class Categories(val value: Set<String>?) : IntentField

    @Immutable
    class Flags(val value: Int) : IntentField

    @Immutable
    class Extras(bundle: Bundle) : IntentField {
        val bundle = Bundle(bundle)

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

private val componentClassNameRegex =
        Regex("\\.?[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*")

internal fun updateIntentField(field: IntentField, intent: Intent): Intent {
    val updatedIntent = Intent(intent)
    when (field) {
        is IntentField.Action -> updatedIntent.action = field.value?.takeIf { it.isNotBlank() }
        is IntentField.Data -> updatedIntent.setDataAndType(
                field.uri?.takeIf { it.toString().isNotBlank() },
                updatedIntent.type
        )
        is IntentField.Categories -> {
            val existing = updatedIntent.categories ?: emptySet()
            val new = field.value ?: emptySet()
            (existing - new).forEach { updatedIntent.removeCategory(it) }
            new.forEach { updatedIntent.addCategory(it) }
        }
        is IntentField.Component -> {
            val component = field.value
            updatedIntent.component = component?.takeIf {
                it.packageName.isNotBlank() || it.className.isNotBlank()
            }
        }
        is IntentField.Extras -> updatedIntent.replaceExtras(Bundle(field.bundle))
        is IntentField.Flags -> updatedIntent.flags = field.value
        is IntentField.MimeType -> updatedIntent.setDataAndType(
                updatedIntent.data,
                field.value?.takeIf { it.isNotBlank() }
        )
        IntentField.None -> throw IllegalArgumentException("Cannot update an intent with an empty field")
    }
    return updatedIntent
}