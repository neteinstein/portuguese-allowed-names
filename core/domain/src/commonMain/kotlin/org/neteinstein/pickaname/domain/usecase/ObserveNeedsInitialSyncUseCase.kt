package org.neteinstein.pickaname.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.neteinstein.pickaname.domain.repository.NameRepository

/**
 * Observes whether the local names database still needs an initial population, i.e. whether the
 * app should route through the sync/loading screen before showing the names list.
 */
class ObserveNeedsInitialSyncUseCase(private val nameRepository: NameRepository) {
    operator fun invoke(): Flow<Boolean> = nameRepository.observeIsEmpty()
}
