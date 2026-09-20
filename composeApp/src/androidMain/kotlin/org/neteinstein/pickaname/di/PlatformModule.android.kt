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
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.pickaname.data.local.database.AppDatabase
import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.local.database.RoomNameLocalDataSource
import org.neteinstein.pickaname.data.local.datastore.migrateLegacyDataStoreSettings
import org.neteinstein.pickaname.data.parser.PdfTextExtractor
import org.neteinstein.pickaname.data.repository.NameSyncRepositoryImpl
import org.neteinstein.pickaname.domain.repository.NameSyncRepository

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
        val settings = SharedPreferencesSettings(sharedPreferences)
        // Blocking, deliberately: this has to finish before anything reads a setting, and it's a
        // single small file read that only happens for installs predating the Phase 1 store swap
        // (it starts with a File.exists() check and deletes the file once copied, so every other
        // launch pays nothing).
        runBlocking { migrateLegacyDataStoreSettings(androidContext(), settings) }
        settings.toFlowSettings(Dispatchers.Default)
    }

    single { PdfTextExtractor() }

    // Native platforms download and parse the source PDF themselves.
    single<NameSyncRepository> { NameSyncRepositoryImpl(get(), get(), get(), get()) }
}
