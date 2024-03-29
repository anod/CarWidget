package info.anodsplace.carwidget.content.di

import android.appwidget.AppWidgetManager
import android.content.Context
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import coil.ImageLoader
import info.anodsplace.applog.AppLog
import info.anodsplace.carwidget.content.AppCoroutineScope
import info.anodsplace.carwidget.content.BitmapLruCache
import info.anodsplace.carwidget.content.backup.BackupManager
import info.anodsplace.carwidget.content.db.Database
import info.anodsplace.carwidget.content.db.DbShortcutIconLoader
import info.anodsplace.carwidget.content.db.Favorites
import info.anodsplace.carwidget.content.db.ShortcutIconLoader
import info.anodsplace.carwidget.content.db.ShortcutsDatabase
import info.anodsplace.carwidget.content.graphics.AppIconFetcher
import info.anodsplace.carwidget.content.graphics.ShortcutIconRequestHandler
import info.anodsplace.carwidget.content.preferences.AppSettings
import info.anodsplace.carwidget.content.preferences.InCarInterface
import info.anodsplace.carwidget.content.preferences.InCarSettings
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

class AndroidLogger : Logger(Level.DEBUG) {
    override fun display(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> AppLog.d(msg)
            Level.INFO -> AppLog.i(msg)
            Level.ERROR -> AppLog.e(msg)
            Level.WARNING -> AppLog.w(msg)
            Level.NONE -> {}
        }
    }
}

fun createAppModule(): Module = module {
    factory<AppWidgetManager> { AppWidgetManager.getInstance(get()) }
    single<Logger> { AndroidLogger() }
    singleOf(::AppSettings)
    single { AppCoroutineScope() }
    factory {
        val context: Context = get()
        ImageLoader.Builder(context)
            .components {
                add(AppIconFetcher.Factory(context))
                add(ShortcutIconRequestHandler.Factory(
                    db = get(),
                    iconLoader = get(),
                    skinPropertiesFactory = get(),
                    buildProps = get()
                ))
            }
            .build()
    }
    factoryOf(::BackupManager)
    factoryOf(::BitmapLruCache)
    single {
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = get(),
            name = "carhomewidget.db"
        )
        Database(
            driver = driver,
            favoritesAdapter = Favorites.Adapter(
                targetIdAdapter = IntColumnAdapter,
                iconTypeAdapter = IntColumnAdapter,
                itemTypeAdapter = IntColumnAdapter,
                positionAdapter = IntColumnAdapter
            )
        )
    }
    singleOf(::ShortcutsDatabase)
    singleOf(::DbShortcutIconLoader) bind ShortcutIconLoader::class
    singleOf(::InCarSettings) bind InCarInterface::class
}