package info.anodsplace.carwidget.content

import android.appwidget.AppWidgetManager
import androidx.appcompat.app.AppCompatDelegate
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.InCarStorage
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

class AndroidLogger : Logger(Level.DEBUG) {
    override fun log(level: Level, msg: MESSAGE) {
        when(level) {
            Level.DEBUG -> AppLog.d(msg)
            Level.INFO -> AppLog.i(msg)
            Level.ERROR -> AppLog.e(msg)
            Level.NONE -> { }
        }
    }
}

fun createAppModule(): Module = module {
    factory<AppWidgetManager> { AppWidgetManager.getInstance(get()) }
    single<Logger> { AndroidLogger() }
    single { AppSettings(get()) }
    factory<InCarInterface> { InCarStorage.load(get()) }
    factory(named("NightMode")) {
        if (get<AppSettings>().theme == AppSettings.dark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
    }
}