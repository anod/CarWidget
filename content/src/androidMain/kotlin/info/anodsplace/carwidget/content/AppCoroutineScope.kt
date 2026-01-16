package info.anodsplace.carwidget.content

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class AppCoroutineScope(context: CoroutineContext) : CoroutineScope {
    constructor() : this(SupervisorJob() + Dispatchers.Main.immediate)

    override val coroutineContext: CoroutineContext = context
}
