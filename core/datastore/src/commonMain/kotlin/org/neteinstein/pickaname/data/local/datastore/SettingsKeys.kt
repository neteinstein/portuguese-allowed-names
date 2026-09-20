package org.neteinstein.pickaname.data.local.datastore

/**
 * The persisted setting keys, shared between the store itself and the one-time migration from
 * the app's old Preferences DataStore (see `LegacyDataStoreMigration` in `androidMain`). They are
 * the same strings the DataStore version used, which is what makes that migration a plain copy -
 * so don't rename one without migrating it.
 */
internal object SettingsKeys {
    const val SOURCE_URL = "names_source_url"
    const val REFRESH_PERIOD = "names_refresh_period"
    const val LAST_REFRESH_TIMESTAMP = "names_last_refresh_timestamp"
    const val SEARCH_ENGINE = "name_search_engine"
}
