package info.anodsplace.carwidget.content.di

import info.anodsplace.carwidget.content.preferences.WidgetInterface
import info.anodsplace.carwidget.content.preferences.WidgetSettings
import info.anodsplace.carwidget.content.shortcuts.WidgetShortcutsModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun createWidgetInstanceModule(): Module = module {
    scope<AppWidgetIdScope> {
        scopedOf(::WidgetShortcutsModel)
        scopedOf(::WidgetSettings) bind WidgetInterface::class

    }
}