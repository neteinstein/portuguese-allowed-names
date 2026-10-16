package org.neteinstein.pickaname.domain.repository

import kotlinx.coroutines.flow.Flow
import org.neteinstein.pickaname.domain.model.RefreshPeriod

/**
 * Persisted user preferences: the URL the names list PDF is downloaded from, how often it should
 * be automatically re-checked, and when it was last successfully refreshed.
 */
interface SettingsRepository {

    /** Emits the currently configured source URL, defaulting to [org.neteinstein.pickaname.domain.model.NamesSourceDefaults.DEFAULT_SOURCE_URL]. */
    fun observeSourceUrl(): Flow<String>

    suspend fun getSourceUrl(): String

    suspend fun setSourceUrl(url: String)

    suspend fun resetSourceUrlToDefault()

    /** Emits the currently configured auto-refresh cadence, defaulting to [RefreshPeriod.DEFAULT]. */
    fun observeRefreshPeriod(): Flow<RefreshPeriod>

    suspend fun getRefreshPeriod(): RefreshPeriod

    suspend fun setRefreshPeriod(period: RefreshPeriod)

    /**
     * Epoch-millis timestamp of the last successful sync (initial load, manual source change, or
     * automatic refresh), or `null` if one has never happened yet.
     */
    suspend fun getLastRefreshTimestamp(): Long?

    suspend fun setLastRefreshTimestamp(timestampMillis: Long)
}
