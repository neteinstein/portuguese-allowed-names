package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.repository.SettingsRepository

/** Restores the names-source URL to the built-in default. */
class ResetSourceUrlUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke() = settingsRepository.resetSourceUrlToDefault()
}
