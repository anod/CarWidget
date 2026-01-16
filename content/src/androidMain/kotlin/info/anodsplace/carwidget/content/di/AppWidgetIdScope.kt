package info.anodsplace.carwidget.content.di

import android.appwidget.AppWidgetManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.getScopeName
import org.koin.core.scope.Scope
import org.koin.core.scope.ScopeID
import java.io.Closeable

class AppWidgetIdScope(
    val value: Int,
    instance: Int,
    private val existingScope: Scope?
) : KoinScopeComponent, Closeable {
    private val scopeId: ScopeID = scopeId(value, instance)

    init {
        if (existingScope != null && existingScope.id != scopeId) {
            throw IllegalStateException("ExistingScope ID (${existingScope.id}) should match scopeId ($scopeId)")
        }
    }

    override val scope: Scope by lazy { existingScope ?: getKoin().createScope(scopeId, getScopeName(), this) }

    override fun close() {
        if (existingScope == null && scope.isNotClosed()) {
            scope.close()
        }
    }

    companion object {
        const val PREVIEW_ID: Int = -1
        fun scopeId(widgetId: Int, instance: Int): ScopeID = "widget-scope-$widgetId-$instance"
    }
}

val AppWidgetIdScope?.isValid: Boolean
    get() = this != null && this.value != AppWidgetManager.INVALID_APPWIDGET_ID

// +appWidgetId
operator fun AppWidgetIdScope?.unaryPlus(): Int = this?.value ?: AppWidgetManager.INVALID_APPWIDGET_ID

fun KoinComponent.getOrCreateAppWidgetScope(widgetId: Int): AppWidgetIdScope {
    val scopeId = AppWidgetIdScope.scopeId(widgetId, widgetId)
    val existingScope = getKoin().getScopeOrNull(scopeId)
    if (existingScope != null) {
        return AppWidgetIdScope(value = widgetId, instance = widgetId, existingScope = existingScope)
    }
    return AppWidgetIdScope(value = widgetId, instance = widgetId, existingScope = null)
}