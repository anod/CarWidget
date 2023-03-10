package info.anodsplace.carwidget.skin

import info.anodsplace.carwidget.content.preferences.WidgetInterface

class GlossyProperties : BaseProperties() {
    override val name: String = WidgetInterface.SKIN_GLOSSY

    override val settingsButtonRes: Int
        get() = R.drawable.ic_settings

    override val rowLayout: Int
        get() = R.layout.sk_glass_row

    override fun getLayout(number: Int): Int {
        return when (number) {
            2 -> R.layout.sk_glass_2
            4 -> R.layout.sk_glass_4
            8 -> R.layout.sk_glass_8
            10 -> R.layout.sk_glass_10
            else -> R.layout.sk_glass_6
        }
    }
}
