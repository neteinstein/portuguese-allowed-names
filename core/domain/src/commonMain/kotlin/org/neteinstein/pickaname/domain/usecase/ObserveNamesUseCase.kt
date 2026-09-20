package org.neteinstein.pickaname.domain.usecase

import kotlinx.coroutines.flow.Flow
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.repository.NameRepository

/** Observes the names list matching a given [NameFilter], reactively. */
class ObserveNamesUseCase(private val nameRepository: NameRepository) {
    operator fun invoke(filter: NameFilter): Flow<List<NameEntry>> =
        nameRepository.observeNames(filter)
}
