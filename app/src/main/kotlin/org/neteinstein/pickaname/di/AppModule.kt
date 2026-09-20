package org.neteinstein.pickaname.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import org.koin.dsl.module

/**
 * Framework-level singletons shared across the data layer (currently just the shared
 * [HttpClient] used to download the names-list PDF).
 */
val appModule = module {
    single {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
        }
    }
}
