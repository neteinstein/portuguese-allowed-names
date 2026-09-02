package org.neteinstein.pickaname.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.neteinstein.pickaname.domain.model.NamesSourceDefaults
import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/**
 * [SettingsRepository] backed by Preferences DataStore, so the configured source URL, refresh
 * cadence, and last-refresh timestamp all survive process death and app restarts.
 */
class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override fun observeSourceUrl(): Flow<String> =
        dataStore.data.map { prefs ->
            prefs[SOURCE_URL_KEY] ?: NamesSourceDefaults.DEFAULT_SOURCE_URL
        }

    override suspend fun getSourceUrl(): String = observeSourceUrl().first()

    override suspend fun setSourceUrl(url: String) {
        dataStore.edit { prefs -> prefs[SOURCE_URL_KEY] = url }
    }

    override suspend fun resetSourceUrlToDefault() {
        dataStore.edit { prefs -> prefs[SOURCE_URL_KEY] = NamesSourceDefaults.DEFAULT_SOURCE_URL }
    }

    override fun observeRefreshPeriod(): Flow<RefreshPeriod> =
        dataStore.data.map { prefs -> prefs[REFRESH_PERIOD_KEY].toRefreshPeriod() }

    override suspend fun getRefreshPeriod(): RefreshPeriod = observeRefreshPeriod().first()

    override suspend fun setRefreshPeriod(period: RefreshPeriod) {
        dataStore.edit { prefs -> prefs[REFRESH_PERIOD_KEY] = period.name }
    }

    override fun observeSearchEngine(): Flow<SearchEngine> =
        dataStore.data.map { prefs -> prefs[SEARCH_ENGINE_KEY].toSearchEngine() }

    override suspend fun getSearchEngine(): SearchEngine = observeSearchEngine().first()

    override suspend fun setSearchEngine(engine: SearchEngine) {
        dataStore.edit { prefs -> prefs[SEARCH_ENGINE_KEY] = engine.name }
    }

    override suspend fun getLastRefreshTimestamp(): Long? =
        dataStore.data.map { prefs -> prefs[LAST_REFRESH_TIMESTAMP_KEY] }.first()

    override suspend fun setLastRefreshTimestamp(timestampMillis: Long) {
        dataStore.edit { prefs -> prefs[LAST_REFRESH_TIMESTAMP_KEY] = timestampMillis }
    }

    private fun String?.toRefreshPeriod(): RefreshPeriod =
        RefreshPeriod.entries.firstOrNull { it.name == this } ?: RefreshPeriod.DEFAULT

    private fun String?.toSearchEngine(): SearchEngine =
        SearchEngine.entries.firstOrNull { it.name == this } ?: SearchEngine.DEFAULT

    private companion object {
        val SOURCE_URL_KEY = stringPreferencesKey("names_source_url")
        val REFRESH_PERIOD_KEY = stringPreferencesKey("names_refresh_period")
        val LAST_REFRESH_TIMESTAMP_KEY = longPreferencesKey("names_last_refresh_timestamp")
        val SEARCH_ENGINE_KEY = stringPreferencesKey("name_search_engine")
    }
}
