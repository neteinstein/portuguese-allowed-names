package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.repository.SettingsRepository

/** One-shot read of the currently configured names-source URL. */
class GetSourceUrlUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(): String = settingsRepository.getSourceUrl()
}
