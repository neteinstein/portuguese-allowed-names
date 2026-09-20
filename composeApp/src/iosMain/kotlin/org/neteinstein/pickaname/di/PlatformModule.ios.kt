@file:OptIn(com.russhwolf.settings.ExperimentalSettingsApi::class)

package org.neteinstein.pickaname.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.local.database.UserDefaultsNameLocalDataSource
import org.neteinstein.pickaname.data.parser.PdfTextExtractor
import org.neteinstein.pickaname.data.repository.NameSyncRepositoryImpl
import org.neteinstein.pickaname.domain.repository.NameSyncRepository
import platform.Foundation.NSUserDefaults

/**
 * The Apple equivalents of Android's Room/SharedPreferences/CIO stack: the names list and the
 * settings both in `NSUserDefaults`, Ktor's Darwin engine (`NSURLSession` underneath), and
 * PDFKit for text extraction.
 */
actual fun platformModule(): Module = module {
    single {
        HttpClient(Darwin) {
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
        }
    }

    single<NameLocalDataSource> { UserDefaultsNameLocalDataSource() }

    single<FlowSettings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults).toFlowSettings(Dispatchers.Default)
    }

    single { PdfTextExtractor() }

    // Native platforms download and parse the source PDF themselves.
    single<NameSyncRepository> { NameSyncRepositoryImpl(get(), get(), get(), get()) }
}
