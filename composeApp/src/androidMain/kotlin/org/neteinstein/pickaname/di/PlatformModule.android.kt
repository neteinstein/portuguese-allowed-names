@file:OptIn(com.russhwolf.settings.ExperimentalSettingsApi::class)

package org.neteinstein.pickaname.di

import android.content.Context
import androidx.room.Room
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.pickaname.data.local.database.AppDatabase
import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.local.database.RoomNameLocalDataSource
import org.neteinstein.pickaname.data.parser.PdfTextExtractor

private const val SETTINGS_PREFERENCES_NAME = "pick_a_name_settings"

actual fun platformModule(): Module = module {
    single {
        HttpClient(CIO) {
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
        }
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
    single { get<AppDatabase>().nameDao() }
    single<NameLocalDataSource> { RoomNameLocalDataSource(get()) }

    single<FlowSettings> {
        val sharedPreferences = androidContext().getSharedPreferences(
            SETTINGS_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        SharedPreferencesSettings(sharedPreferences).toFlowSettings(Dispatchers.Default)
    }

    single { PdfTextExtractor() }
}
