package info.anodsplace.carwidget.content.di

import android.appwidget.AppWidgetManager
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.scope.Scope
import java.io.Closeable
import kotlin.random.Random

data class AppWidgetIdScope(val value: Int, val instance: Int = Random.nextInt()) : KoinScopeComponent, Closeable {
    override val scope: Scope by lazy { createScope(this) }

    // operator fun unaryPlus() = value

    override fun close() {
        closeScope()
    }

    companion object {
        const val previewId: Int = -1
    }
}

val AppWidgetIdScope?.isValid: Boolean
    get() = this != null && this.value != AppWidgetManager.INVALID_APPWIDGET_ID

// +appWidgetId
operator fun AppWidgetIdScope?.unaryPlus(): Int = this?.value ?: AppWidgetManager.INVALID_APPWIDGET_ID