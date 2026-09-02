package org.neteinstein.pickaname.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.repository.SettingsRepository

/** Live stream of the currently configured name-meaning search engine. */
class ObserveSearchEngineUseCase(private val settingsRepository: SettingsRepository) {
    operator fun invoke(): Flow<SearchEngine> = settingsRepository.observeSearchEngine()
}
