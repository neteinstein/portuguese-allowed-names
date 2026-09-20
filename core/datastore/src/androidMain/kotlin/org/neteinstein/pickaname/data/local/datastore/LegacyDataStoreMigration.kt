package org.neteinstein.pickaname.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.first
import java.io.File

/**
 * One-time copy of the settings this app stored in Preferences DataStore before it moved to
 * multiplatform-settings (MIGRATION_PLAN.md Phase 1).
 *
 * Without this, everyone who already had the app installed silently lost their configured
 * source URL, refresh cadence and search engine on the first launch after that update, and the
 * cleared last-refresh timestamp forced an extra sync. The keys themselves never changed - only
 * the storage backend did - so this is a straight copy.
 *
 * Runs at most once per install: it does nothing when the legacy file is absent (every new
 * install, and every launch after a successful migration, since the file is deleted at the end).
 */
suspend fun migrateLegacyDataStoreSettings(context: Context, settings: Settings) {
    // The original code passed a name that already ended in ".preferences_pb" to
    // preferencesDataStoreFile(), which appends the same suffix again - so the real file on disk
    // has it twice. Calling the same helper the same way is what finds it.
    migrateLegacyDataStoreSettings(
        legacyFile = context.preferencesDataStoreFile(LEGACY_DATASTORE_NAME),
        settings = settings
    )
}

internal suspend fun migrateLegacyDataStoreSettings(legacyFile: File, settings: Settings) {
    if (!legacyFile.exists()) return

    val legacyPreferences = runCatching {
        PreferenceDataStoreFactory.create(produceFile = { legacyFile }).data.first()
    }.getOrNull()
        // An unreadable legacy file is not worth crashing a launch over: the app just carries on
        // with defaults, as it did before this migration existed. The file is deliberately left
        // in place rather than deleted - re-reading it next launch costs nothing, and deleting
        // data we failed to read once is exactly the mistake this whole migration exists to fix.
        ?: return

    // putIfAbsent semantics throughout: anything already written through the new store is newer
    // than the legacy file and must win.
    legacyPreferences[stringPreferencesKey(SettingsKeys.SOURCE_URL)]?.let { url ->
        if (!settings.hasKey(SettingsKeys.SOURCE_URL)) settings.putString(SettingsKeys.SOURCE_URL, url)
    }
    legacyPreferences[stringPreferencesKey(SettingsKeys.REFRESH_PERIOD)]?.let { period ->
        if (!settings.hasKey(SettingsKeys.REFRESH_PERIOD)) settings.putString(SettingsKeys.REFRESH_PERIOD, period)
    }
    legacyPreferences[stringPreferencesKey(SettingsKeys.SEARCH_ENGINE)]?.let { engine ->
        if (!settings.hasKey(SettingsKeys.SEARCH_ENGINE)) settings.putString(SettingsKeys.SEARCH_ENGINE, engine)
    }
    legacyPreferences[longPreferencesKey(SettingsKeys.LAST_REFRESH_TIMESTAMP)]?.let { timestamp ->
        if (!settings.hasKey(SettingsKeys.LAST_REFRESH_TIMESTAMP)) {
            settings.putLong(SettingsKeys.LAST_REFRESH_TIMESTAMP, timestamp)
        }
    }

    legacyFile.delete()
}

private const val LEGACY_DATASTORE_NAME = "pick_a_name_settings.preferences_pb"
