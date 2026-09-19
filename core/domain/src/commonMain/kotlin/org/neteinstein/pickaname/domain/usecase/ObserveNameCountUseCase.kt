package org.neteinstein.pickaname.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.repository.NameRepository

/** Observes how many names match a given [NameFilter], reactively. */
class ObserveNameCountUseCase(private val nameRepository: NameRepository) {
    operator fun invoke(filter: NameFilter): Flow<Int> =
        nameRepository.observeNameCount(filter)
}
