package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/** One-shot read of the currently configured name-meaning search engine. */
class GetSearchEngineUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(): SearchEngine = settingsRepository.getSearchEngine()
}
