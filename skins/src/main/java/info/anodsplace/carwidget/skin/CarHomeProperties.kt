package info.anodsplace.carwidget.skin

import info.anodsplace.carwidget.content.preferences.WidgetInterface

class CarHomeProperties : BaseProperties() {
    override val name: String = WidgetInterface.SKIN_CARHOME

    override val settingsButtonRes: Int
        get() = R.drawable.ic_settings

    override val rowLayout: Int
        get() = R.layout.sk_carhome_row


    override fun getLayout(number: Int): Int {
        return when (number) {
            2 -> R.layout.sk_carhome_2
            4 -> R.layout.sk_carhome_4
            8 -> R.layout.sk_carhome_8
            10 -> R.layout.sk_carhome_10
            else -> R.layout.sk_carhome_6
        }
    }
}
