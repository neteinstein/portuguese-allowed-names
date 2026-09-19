package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/** Persists how often the app should automatically re-check the names source. */
class UpdateRefreshPeriodUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(period: RefreshPeriod) = settingsRepository.setRefreshPeriod(period)
}
