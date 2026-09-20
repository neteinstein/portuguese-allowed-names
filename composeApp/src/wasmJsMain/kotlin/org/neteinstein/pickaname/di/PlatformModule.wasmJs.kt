@file:OptIn(com.russhwolf.settings.ExperimentalSettingsApi::class)

package org.neteinstein.pickaname.di

import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.observable.makeObservable
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module
import org.neteinstein.pickaname.data.local.database.LocalStorageNameLocalDataSource
import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.parser.PdfTextExtractor
import org.neteinstein.pickaname.data.repository.SnapshotNameSyncRepository
import org.neteinstein.pickaname.domain.repository.NameSyncRepository

/**
 * The browser equivalents of Android's Room/SharedPreferences/CIO stack: the names list in
 * `localStorage` (Room has no wasmJs support - see MIGRATION_PLAN.md risk R1), settings in
 * `localStorage` too via multiplatform-settings' own `StorageSettings`, and Ktor's JS engine
 * (which is `fetch` underneath).
 */
actual fun platformModule(): Module = module {
    single {
        HttpClient(Js) {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
            }
        }
    }

    single<NameLocalDataSource> { LocalStorageNameLocalDataSource() }

    // StorageSettings isn't observable on its own (localStorage has no change callback for
    // same-document writes), so makeObservable() adds the in-process listener layer FlowSettings
    // needs - enough for this app, where only the app itself writes these keys.
    single<FlowSettings> { StorageSettings().makeObservable().toFlowSettings(Dispatchers.Default) }

    single { PdfTextExtractor() }

    // The web build syncs from the snapshot CI publishes next to it rather than downloading the
    // source PDF: the browser would refuse that request outright (no CORS header - risk R3), and
    // this spares every visitor a 2.9 MB PDF parse. PdfTextExtractor above stays registered for
    // a user-supplied, CORS-enabled URL.
    single<NameSyncRepository> { SnapshotNameSyncRepository(get(), get()) }
}
