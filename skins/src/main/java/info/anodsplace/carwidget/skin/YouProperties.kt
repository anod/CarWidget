package info.anodsplace.carwidget.skin

import android.content.Context
import android.content.Intent
import info.anodsplace.carwidget.content.preferences.WidgetInterface

class YouProperties(context: Context) : BaseProperties() {

    override val name: String = WidgetInterface.SKIN_YOU

    private val iconResources = mapOf(
        R.drawable.ic_shortcut_call to R.drawable.ic_shortcut_call_primary,
        R.drawable.ic_shortcut_play to R.drawable.ic_shortcut_play_primary,
        R.drawable.ic_shortcut_next to R.drawable.ic_shortcut_next_primary,
        R.drawable.ic_shortcut_previous to R.drawable.ic_shortcut_previous_primary,
    )
    private val primaryColorIcons by lazy {
            iconResources.keys
                .associateBy { resId -> context.resources.getResourceName(resId).substringAfterLast("drawable/") }

    }

    override val backgroundId: Int
        get() = R.id.background

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

    override val buttonAlternativeHiddenResId: Int
        get() = R.drawable.you_clear_24

    override val iconPaddingRes: Int
        get() = 0

    override val fontColorRes: Int
        get() = 0

    override fun getLayout(number: Int): Int {
        return when (number) {
            2 -> R.layout.sk_you_2
            4 -> R.layout.sk_you_4
            8 -> R.layout.sk_you_8
            10 -> R.layout.sk_you_10
            else -> R.layout.sk_you_6
        }
    }

    override fun iconResourceTint(iconResource: Intent.ShortcutIconResource?): Int {
        iconResource ?: return 0
        if (iconResource.packageName.startsWith("com.anod.car.home")) {
            val resourceId = iconResource.resourceName.substringAfterLast("drawable/")
            return if (primaryColorIcons.contains(resourceId)) R.color.button_tint_primary else 0
        }
        return 0
    }

    override fun supportsWidgetButton1(): Boolean {
        return true
    }
}