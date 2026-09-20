@file:OptIn(com.russhwolf.settings.ExperimentalSettingsApi::class)

package org.neteinstein.pickaname.data.local.datastore

import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.neteinstein.pickaname.domain.model.NamesSourceDefaults
import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/**
 * [SettingsRepository] backed by [FlowSettings], so the configured source URL, refresh cadence,
 * and last-refresh timestamp all survive process death and app restarts.
 */
class SettingsRepositoryImpl(
    private val settings: FlowSettings
) : SettingsRepository {

    override fun observeSourceUrl(): Flow<String> =
        settings.getStringFlow(SOURCE_URL_KEY, NamesSourceDefaults.DEFAULT_SOURCE_URL)

    override suspend fun getSourceUrl(): String = observeSourceUrl().first()

    override suspend fun setSourceUrl(url: String) {
        settings.putString(SOURCE_URL_KEY, url)
    }

    override suspend fun resetSourceUrlToDefault() {
        settings.putString(SOURCE_URL_KEY, NamesSourceDefaults.DEFAULT_SOURCE_URL)
    }

    override fun observeRefreshPeriod(): Flow<RefreshPeriod> =
        settings.getStringOrNullFlow(REFRESH_PERIOD_KEY).map { it.toRefreshPeriod() }

    override suspend fun getRefreshPeriod(): RefreshPeriod = observeRefreshPeriod().first()

    override suspend fun setRefreshPeriod(period: RefreshPeriod) {
        settings.putString(REFRESH_PERIOD_KEY, period.name)
    }

    override fun observeSearchEngine(): Flow<SearchEngine> =
        settings.getStringOrNullFlow(SEARCH_ENGINE_KEY).map { it.toSearchEngine() }

    override suspend fun getSearchEngine(): SearchEngine = observeSearchEngine().first()

    override suspend fun setSearchEngine(engine: SearchEngine) {
        settings.putString(SEARCH_ENGINE_KEY, engine.name)
    }

    override suspend fun getLastRefreshTimestamp(): Long? =
        settings.getLongOrNull(LAST_REFRESH_TIMESTAMP_KEY)

    override suspend fun setLastRefreshTimestamp(timestampMillis: Long) {
        settings.putLong(LAST_REFRESH_TIMESTAMP_KEY, timestampMillis)
    }

    private fun String?.toRefreshPeriod(): RefreshPeriod =
        RefreshPeriod.entries.firstOrNull { it.name == this } ?: RefreshPeriod.DEFAULT

    private fun String?.toSearchEngine(): SearchEngine =
        SearchEngine.entries.firstOrNull { it.name == this } ?: SearchEngine.DEFAULT

    private companion object {
        const val SOURCE_URL_KEY = "names_source_url"
        const val REFRESH_PERIOD_KEY = "names_refresh_period"
        const val LAST_REFRESH_TIMESTAMP_KEY = "names_last_refresh_timestamp"
        const val SEARCH_ENGINE_KEY = "name_search_engine"
    }
}
