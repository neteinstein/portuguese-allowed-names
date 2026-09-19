package org.neteinstein.pickaname.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/** Observes the currently configured names-source URL. */
class ObserveSourceUrlUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(): Flow<String> = settingsRepository.observeSourceUrl()
}
