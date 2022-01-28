package info.anodsplace.carwidget.content

import android.appwidget.AppWidgetManager
import com.squareup.picasso.Picasso
import com.squareup.sqldelight.android.AndroidSqliteDriver
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.graphics.PackageIconRequestHandler
import info.anodsplace.carwidget.content.graphics.ShortcutIconRequestHandler
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.InCarStorage
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.module.Module
import org.koin.core.scope.get
import org.koin.dsl.module

class AndroidLogger : Logger(Level.DEBUG) {
    override fun log(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> AppLog.d(msg)
            Level.INFO -> AppLog.i(msg)
            Level.ERROR -> AppLog.e(msg)
            Level.NONE -> {
            }
        }
    }
}

fun createAppModule(): Module = module {
    factory<AppWidgetManager> { AppWidgetManager.getInstance(get()) }
    single<Logger> { AndroidLogger() }
    single { AppSettings(get()) }
    single { AppCoroutineScope() }
    factory { Version(get()) }
    factory {
        Picasso.Builder(get())
            .addRequestHandler(ShortcutIconRequestHandler(get(), get(), get()))
            .addRequestHandler(PackageIconRequestHandler(get()))
            .build()
    }
    factory { BitmapLruCache(get()) }
    single {
        val driver = AndroidSqliteDriver(
                schema = Database.Schema,
                context = get(),
                name = "carhomewidget.db"
        )
        Database(driver)
    }
    single { ShortcutsDatabase(get(), get()) }
    single { ShortcutIconLoader(get(), get()) }
    factory<InCarInterface> { InCarStorage.load(get()) }
}