package org.neteinstein.pickaname.di

import org.koin.core.module.Module

/**
 * Everything the data layer needs that only a platform can build: the Ktor engine, the settings
 * store, the local names store, and the PDF text extractor.
 *
 * Android builds these from a `Context` (Room, SharedPreferences, CIO); web from browser APIs
 * (`localStorage`, the JS fetch engine). Every other module - repositories, use cases, view
 * models - is identical on both and lives in [appModules].
 */
expect fun platformModule(): Module

/** The whole Koin graph, in the order a platform shell should register it. */
fun appModules(): List<Module> = listOf(
    platformModule(),
    repositoryModule,
    useCaseModule,
    viewModelModule
)
