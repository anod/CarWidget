package com.anod.car.home

import com.anod.car.home.appwidget.WidgetViewBuilder
import com.anod.car.home.prefs.SkinPreviewIntentFactory
import info.anodsplace.carwidget.appwidget.DummyWidgetView
import info.anodsplace.carwidget.appwidget.PendingIntentFactory
import info.anodsplace.carwidget.appwidget.PreviewPendingIntentFactory
import info.anodsplace.carwidget.appwidget.WidgetView
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.db.Shortcuts
import info.anodsplace.carwidget.content.di.AppWidgetIdScope
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.DummyWidgetShortcutsModel
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun createWidgetInstanceModule(): Module = module {
    scope<AppWidgetIdScope> {
        scopedOf(::WidgetShortcutsModel) bind Shortcuts::class
        scopedOf(::WidgetSettings) bind WidgetInterface::class
        factory<PreviewPendingIntentFactory> {
            SkinPreviewIntentFactory(get(), get())
        }

        factory<WidgetView> { (
            bitmapMemoryCache: BitmapLruCache?,
            pendingIntentFactory: PendingIntentFactory,
            widgetButtonAlternativeHidden: Boolean,
            overrideSkin: String?,
            overrideCount: Int?
        ) ->
            WidgetViewBuilder(
                context = get(),
                iconLoader = get(),
                appWidgetId = get<AppWidgetIdScope>().value,
                bitmapMemoryCache = bitmapMemoryCache,
                pendingIntentFactory = pendingIntentFactory,
                widgetButtonAlternativeHidden = widgetButtonAlternativeHidden,
                overrideSkin = overrideSkin,
                overrideCount = overrideCount,
                widgetSettings = get(),
                inCarSettings = get(),
                shortcutsModel = get(),
                koin = getKoin()
            )
        }
    }
    factory<DummyWidgetView> { (
                                   bitmapMemoryCache: BitmapLruCache?,
                                   pendingIntentFactory: PendingIntentFactory,
                                   widgetButtonAlternativeHidden: Boolean,
                                   overrideSkin: String?,
                                   overrideCount: Int?
                               ) ->
        WidgetViewBuilder(
            context = get(),
            iconLoader = get(),
            appWidgetId = AppWidgetIdScope.previewId,
            widgetSettings = WidgetInterface.NoOp(),
            inCarSettings = InCarInterface.NoOp(),
            koin = getKoin(),
            shortcutsModel = DummyWidgetShortcutsModel(context = get(), size = overrideCount ?: 4),
            bitmapMemoryCache = bitmapMemoryCache,
            pendingIntentFactory = pendingIntentFactory,
            widgetButtonAlternativeHidden = widgetButtonAlternativeHidden,
            overrideSkin = overrideSkin,
            overrideCount = overrideCount
        )
    }
}