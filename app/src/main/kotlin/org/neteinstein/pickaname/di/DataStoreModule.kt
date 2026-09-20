@file:OptIn(com.russhwolf.settings.ExperimentalSettingsApi::class)

package org.neteinstein.pickaname.di

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val SETTINGS_PREFERENCES_NAME = "pick_a_name_settings"

/**
 * Settings storage used to persist the configurable names-source URL and related preferences.
 */
val dataStoreModule = module {
    single<FlowSettings> {
        val sharedPreferences = androidContext().getSharedPreferences(
            SETTINGS_PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        SharedPreferencesSettings(sharedPreferences).toFlowSettings(Dispatchers.Default)
    }
}
