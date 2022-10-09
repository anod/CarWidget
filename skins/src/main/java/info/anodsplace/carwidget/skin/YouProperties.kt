package info.anodsplace.carwidget.skin

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import info.anodsplace.carwidget.content.db.ShortcutIconConverter
import info.anodsplace.carwidget.content.preferences.WidgetInterface

class YouProperties(context: Context) : BaseProperties() {
    private val widgetContext = ContextThemeWrapper(context, R.style.AppWidgetContainer)

    override val name: String = WidgetInterface.SKIN_YOU

    private val primaryColorIcons by lazy {
            listOf(
                R.drawable.ic_shortcut_call,
                R.drawable.ic_shortcut_play,
                R.drawable.ic_shortcut_next,
                R.drawable.ic_shortcut_previous,
            )
            .map { resId -> context.resources.getResourceName(resId) }
            .associateWith { true }
    }

    private val primaryColorStateList: ColorStateList by lazy {
        widgetContext.resources.getColorStateList(R.color.button_tint_primary, widgetContext.theme)
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

    override fun iconResourceTint(iconResource: Intent.ShortcutIconResource?): ColorStateList? {
        iconResource ?: return null
        if (primaryColorIcons.containsKey(iconResource.resourceName)) {
            return primaryColorStateList
        }
        return null
    }

    override fun hasWidgetButton1(): Boolean {
        return true
    }
}