package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/** Persists which search engine should be used to look up what a name means. */
class UpdateSearchEngineUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(engine: SearchEngine) = settingsRepository.setSearchEngine(engine)
}
