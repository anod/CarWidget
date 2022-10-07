package info.anodsplace.carwidget.skin

import android.content.Context
import info.anodsplace.carwidget.content.db.ShortcutIconConverter
import info.anodsplace.carwidget.content.preferences.WidgetInterface

class YouProperties(private val context: Context) : BaseProperties() {
    override val name: String = WidgetInterface.SKIN_YOU

    override val iconConverter: ShortcutIconConverter
        get() {
            val replaceResources = mapOf(
                    R.drawable.ic_shortcut_call to R.drawable.ic_shortcut_call_primary,
                    R.drawable.ic_shortcut_play to R.drawable.ic_shortcut_play_primary,
                    R.drawable.ic_shortcut_next to R.drawable.ic_shortcut_next_primary,
                    R.drawable.ic_shortcut_previous to R.drawable.ic_shortcut_previous_primary
            ).entries.associate { entry ->
                Pair(
                        context.resources.getResourceName(entry.key),
                        context.resources.getResourceName(entry.value)
                )
            }
            return ShortcutIconConverter.Default(
                    context = context,
                    replaceResources = replaceResources
            )
        }

    override val inCarButtonExitRes: Int
        get() = R.drawable.you_wheel_exit

    override val inCarButtonEnterRes: Int
        get() = R.drawable.you_wheel

    override val setShortcutRes: Int
        get() = R.drawable.you_add_24

    override val settingsButtonRes: Int
        get() = R.drawable.you_edit_24

    override val rowLayout: Int
        get() = R.layout.sk_you_row

    override val iconPaddingRes: Int
        get() = 0

    override val fontColorRes: Int
        get() = 0

    override fun getLayout(number: Int): Int {
        return when (number) {
            4 -> R.layout.sk_you_4
            8 -> R.layout.sk_you_8
            10 -> R.layout.sk_you_10
            else -> R.layout.sk_you_6
        }
    }

    override fun hasWidgetButton1(): Boolean {
        return true
    }
}