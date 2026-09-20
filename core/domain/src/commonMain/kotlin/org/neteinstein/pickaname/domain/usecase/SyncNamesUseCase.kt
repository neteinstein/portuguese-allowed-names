package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.repository.NameSyncRepository
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/**
 * Downloads the names list from the given URL, parses it, and purges + repopulates the local
 * database. Used for the first-run load, whenever the user changes the source URL, and by the
 * periodic auto-refresh check ([RefreshNamesIfDueUseCase]).
 *
 * On success, stamps "now" as the last-refresh timestamp so [RefreshNamesIfDueUseCase] knows
 * when the next automatic refresh is due. A failure leaves the timestamp untouched, so the next
 * check (next app open) retries instead of silently skipping ahead.
 */
class SyncNamesUseCase(
    private val nameSyncRepository: NameSyncRepository,
    private val settingsRepository: SettingsRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis
) {
    suspend operator fun invoke(url: String): SyncOutcome {
        val outcome = nameSyncRepository.syncFromUrl(url)
        if (outcome is SyncOutcome.Success) {
            settingsRepository.setLastRefreshTimestamp(currentTimeMillis())
        }
        return outcome
    }
}
