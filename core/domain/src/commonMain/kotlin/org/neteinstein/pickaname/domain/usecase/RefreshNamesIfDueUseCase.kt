package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.model.AutoRefreshResult
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/**
 * Runs once per app open, when the database is already populated (so no initial sync is
 * needed): if the configured [org.neteinstein.pickaname.domain.model.RefreshPeriod] has elapsed
 * since the last successful sync - or none has ever happened - re-downloads and re-parses the
 * names source via [SyncNamesUseCase].
 *
 * The local database is only purged once that fetch and parse succeed (see
 * [org.neteinstein.pickaname.domain.repository.NameSyncRepository]), so a failed check leaves
 * existing data untouched and is simply retried on the next app open.
 */
class RefreshNamesIfDueUseCase(
    private val settingsRepository: SettingsRepository,
    private val syncNamesUseCase: SyncNamesUseCase,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(): AutoRefreshResult {
        val lastRefresh = settingsRepository.getLastRefreshTimestamp()
        val period = settingsRepository.getRefreshPeriod()
        val isDue = lastRefresh == null || currentTimeMillis() - lastRefresh >= period.durationMillis
        if (!isDue) return AutoRefreshResult.NotDue

        val url = settingsRepository.getSourceUrl()
        return when (val outcome = syncNamesUseCase(url)) {
            is SyncOutcome.Success -> AutoRefreshResult.Refreshed(outcome.namesLoaded)
            is SyncOutcome.Error -> AutoRefreshResult.Failed(outcome.reason)
        }
    }
}
