package info.anodsplace.carwidget.skin

import info.anodsplace.carwidget.content.IconBackgroundProcessor
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.skin.icon.MetroBackgroundProcessor

class MetroProperties : BaseProperties() {
    override val name: String = WidgetInterface.SKIN_WINDOWS7

    override val inCarButtonExitRes: Int
        get() = R.drawable.ic_incar_exit_win7

    override val inCarButtonEnterRes: Int
        get() = R.drawable.ic_incar_enter_win7

    override val iconPaddingRes: Int
        get() = 0

    override val settingsButtonRes: Int
        get() = R.drawable.ic_settings_win7

    override val rowLayout: Int
        get() = R.layout.sk_windows7_row

    override val backgroundProcessor: IconBackgroundProcessor
        get() = MetroBackgroundProcessor()

    override fun getLayout(number: Int): Int {
        return when (number) {
            2 -> R.layout.sk_windows7_2
            4 -> R.layout.sk_windows7_4
            8 -> R.layout.sk_windows7_8
            10 -> R.layout.sk_windows7_10
            else -> R.layout.sk_windows7_6
        }
    }
}