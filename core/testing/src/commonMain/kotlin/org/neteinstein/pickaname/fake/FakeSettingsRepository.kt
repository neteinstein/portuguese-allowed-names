package org.neteinstein.pickaname.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.neteinstein.pickaname.domain.model.NamesSourceDefaults
import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/**
 * In-memory [SettingsRepository] for tests, behaving like the real one (defaults when unset,
 * observers see writes).
 *
 * Hand-written rather than mocked because these tests live in `commonTest` and run on wasmJs and
 * iOS too, where MockK doesn't exist.
 */
class FakeSettingsRepository(
    sourceUrl: String = NamesSourceDefaults.DEFAULT_SOURCE_URL,
    refreshPeriod: RefreshPeriod = RefreshPeriod.DEFAULT,
    searchEngine: SearchEngine = SearchEngine.DEFAULT,
    lastRefreshTimestamp: Long? = null
) : SettingsRepository {

    private val sourceUrlFlow = MutableStateFlow(sourceUrl)
    private val refreshPeriodFlow = MutableStateFlow(refreshPeriod)
    private val searchEngineFlow = MutableStateFlow(searchEngine)

    /** Exposed so tests can assert on what was written without another round trip. */
    var lastRefreshTimestamp: Long? = lastRefreshTimestamp
        private set

    override fun observeSourceUrl(): Flow<String> = sourceUrlFlow.asStateFlow()

    override suspend fun getSourceUrl(): String = sourceUrlFlow.value

    override suspend fun setSourceUrl(url: String) {
        sourceUrlFlow.value = url
    }

    override suspend fun resetSourceUrlToDefault() {
        sourceUrlFlow.value = NamesSourceDefaults.DEFAULT_SOURCE_URL
    }

    override fun observeRefreshPeriod(): Flow<RefreshPeriod> = refreshPeriodFlow.asStateFlow()

    override suspend fun getRefreshPeriod(): RefreshPeriod = refreshPeriodFlow.value

    override suspend fun setRefreshPeriod(period: RefreshPeriod) {
        refreshPeriodFlow.value = period
    }

    override fun observeSearchEngine(): Flow<SearchEngine> = searchEngineFlow.asStateFlow()

    override suspend fun getSearchEngine(): SearchEngine = searchEngineFlow.value

    override suspend fun setSearchEngine(engine: SearchEngine) {
        searchEngineFlow.value = engine
    }

    override suspend fun getLastRefreshTimestamp(): Long? = lastRefreshTimestamp

    override suspend fun setLastRefreshTimestamp(timestampMillis: Long) {
        lastRefreshTimestamp = timestampMillis
    }
}
