package info.anodsplace.carwidget.content

interface BroadcastServiceManager {
    val isServiceRequired: Boolean
    val isServiceRunning: Boolean

    fun registerBroadcastService()

    fun startService()

    fun stopService()
}