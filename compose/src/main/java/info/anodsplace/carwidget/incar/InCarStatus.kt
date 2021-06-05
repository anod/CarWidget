package info.anodsplace.carwidget.incar

import info.anodsplace.carwidget.R
import info.anodsplace.carwidget.appwidget.WidgetIds
import info.anodsplace.carwidget.content.InCarStatus
import info.anodsplace.carwidget.content.InCarStatus.Companion.DISABLED
import info.anodsplace.carwidget.content.InCarStatus.Companion.ENABLED
import info.anodsplace.carwidget.content.InCarStatus.Companion.NOT_ACTIVE
import info.anodsplace.carwidget.content.Version
import info.anodsplace.carwidget.content.preferences.InCarInterface

class InCarStatus(
    private val widgetIds: WidgetIds,
    private val version: Version,
    private val serviceRequired: () -> Boolean,
    private val serviceRunning: () -> Boolean,
    private val modeEventsState: () -> List<InCarStatus.EventState>,
    private val settings: InCarInterface
) : InCarStatus {

    override val value: Int by lazy { calc(widgetIds.getAllWidgetIds().size, version, settings) }
    override val isEnabled: Boolean
        get() = value == ENABLED
    override val isServiceRequired: Boolean
        get() = serviceRequired()
    override val isServiceRunning: Boolean
        get() = serviceRunning()

    override fun eventsState(): List<InCarStatus.EventState> {
        return modeEventsState()
    }

    companion object {
        private fun calc(widgetsCount: Int, version: Version, settings: InCarInterface): Int {
            if (widgetsCount == 0) {
                return NOT_ACTIVE
            }
            return if (version.isProOrTrial) {
                if (settings.isInCarEnabled) {
                    ENABLED
                } else {
                    DISABLED
                }
            } else DISABLED
        }
    }

    override val resId: Int
        get() {
            if (value == NOT_ACTIVE) {
                return R.string.not_active
            }
            return if (value == ENABLED) {
                R.string.enabled
            } else {
                R.string.disabled
            }
        }
}
