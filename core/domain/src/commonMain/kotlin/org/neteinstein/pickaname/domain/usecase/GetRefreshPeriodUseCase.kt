package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/** One-shot read of the currently configured auto-refresh cadence. */
class GetRefreshPeriodUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(): RefreshPeriod = settingsRepository.getRefreshPeriod()
}
