package info.anodsplace.carwidget.skin

import info.anodsplace.carwidget.content.preferences.WidgetInterface

class HoloProperties : BaseProperties() {
    override val name: String = WidgetInterface.SKIN_HOLO

    override val inCarButtonExitRes: Int
        get() = R.drawable.ic_incar_exit_holo

    override val inCarButtonEnterRes: Int
        get() = R.drawable.ic_incar_enter_holo

    override val setShortcutRes: Int
        get() = R.drawable.ic_add_shortcut_holo

    override val settingsButtonRes: Int
        get() = R.drawable.ic_holo_settings

    override val rowLayout: Int
        get() = R.layout.sk_holo_row

    override fun getLayout(number: Int): Int {
        return when (number) {
            2 -> R.layout.sk_holo_2
            4 -> R.layout.sk_holo_4
            8 -> R.layout.sk_holo_8
            10 -> R.layout.sk_holo_10
            12 -> R.layout.sk_holo_12
            14 -> R.layout.sk_holo_14
            else -> R.layout.sk_holo_6
        }
    }
}
