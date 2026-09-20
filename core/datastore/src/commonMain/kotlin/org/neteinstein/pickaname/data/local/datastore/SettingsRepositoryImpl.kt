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
        settings.getStringFlow(SettingsKeys.SOURCE_URL, NamesSourceDefaults.DEFAULT_SOURCE_URL)

    override suspend fun getSourceUrl(): String = observeSourceUrl().first()

    override suspend fun setSourceUrl(url: String) {
        settings.putString(SettingsKeys.SOURCE_URL, url)
    }

    override suspend fun resetSourceUrlToDefault() {
        settings.putString(SettingsKeys.SOURCE_URL, NamesSourceDefaults.DEFAULT_SOURCE_URL)
    }

    override fun observeRefreshPeriod(): Flow<RefreshPeriod> =
        settings.getStringOrNullFlow(SettingsKeys.REFRESH_PERIOD).map { it.toRefreshPeriod() }

    override suspend fun getRefreshPeriod(): RefreshPeriod = observeRefreshPeriod().first()

    override suspend fun setRefreshPeriod(period: RefreshPeriod) {
        settings.putString(SettingsKeys.REFRESH_PERIOD, period.name)
    }

    override fun observeSearchEngine(): Flow<SearchEngine> =
        settings.getStringOrNullFlow(SettingsKeys.SEARCH_ENGINE).map { it.toSearchEngine() }

    override suspend fun getSearchEngine(): SearchEngine = observeSearchEngine().first()

    override suspend fun setSearchEngine(engine: SearchEngine) {
        settings.putString(SettingsKeys.SEARCH_ENGINE, engine.name)
    }

    override suspend fun getLastRefreshTimestamp(): Long? =
        settings.getLongOrNull(SettingsKeys.LAST_REFRESH_TIMESTAMP)

    override suspend fun setLastRefreshTimestamp(timestampMillis: Long) {
        settings.putLong(SettingsKeys.LAST_REFRESH_TIMESTAMP, timestampMillis)
    }

    private fun String?.toRefreshPeriod(): RefreshPeriod =
        RefreshPeriod.entries.firstOrNull { it.name == this } ?: RefreshPeriod.DEFAULT

    private fun String?.toSearchEngine(): SearchEngine =
        SearchEngine.entries.firstOrNull { it.name == this } ?: SearchEngine.DEFAULT

}
