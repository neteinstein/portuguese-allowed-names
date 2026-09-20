package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.repository.SettingsRepository

/**
 * When the names list was last loaded successfully, or null if it never has been. Surfaced in
 * Settings on platforms that read a published snapshot rather than syncing the source
 * themselves, where "how current is this list?" is the only question left to answer.
 */
class GetLastRefreshTimestampUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(): Long? = settingsRepository.getLastRefreshTimestamp()
}
