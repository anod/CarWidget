package info.anodsplace.carwidget.content

interface InCarStatus {
    val value: Int
    val isEnabled: Boolean
    val resId: Int
    fun eventsState(): List<EventState>

    data class EventState(
        val id: Int,
        val enabled: Boolean,
        val active: Boolean
    )

    companion object {
        const val NOT_ACTIVE = 0
        const val ENABLED = 1
        const val DISABLED = 2
    }
}