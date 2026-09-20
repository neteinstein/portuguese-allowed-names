package org.neteinstein.pickaname.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.mapper.toDomain
import org.neteinstein.pickaname.data.mapper.toEntityCode
import org.neteinstein.pickaname.data.mapper.toFilterInitial
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.model.TraditionalNameRules
import org.neteinstein.pickaname.domain.repository.NameRepository

class NameRepositoryImpl(
    private val nameLocalDataSource: NameLocalDataSource
) : NameRepository {

    override fun observeNames(filter: NameFilter): Flow<List<NameEntry>> {
        val recordsFlow = nameLocalDataSource.observeNames(
            gender = filter.gender?.toEntityCode(),
            initial = filter.initial?.let { "$it".toFilterInitial() },
            query = filter.query.trim().ifBlank { null }
        )
        // TraditionalNameRules' phonotactic checks aren't expressible as SQL, so traditionalOnly
        // is applied in-memory; the both-genders cross-reference is only fetched when it's active.
        return if (filter.traditionalOnly) {
            combine(recordsFlow, nameLocalDataSource.observeNamesUsedByBothGenders()) { records, bothGenderNames ->
                val bothGenderSet = bothGenderNames.toHashSet()
                records.map { it.toDomain() }.filter {
                    TraditionalNameRules.isTraditional(it.name, isUsedByBothGenders = it.name in bothGenderSet)
                }
            }
        } else {
            recordsFlow.map { records -> records.map { it.toDomain() } }
        }
    }

    override fun observeNameCount(filter: NameFilter): Flow<Int> =
        if (filter.traditionalOnly) {
            observeNames(filter).map { it.size }
        } else {
            nameLocalDataSource.observeCount(
                gender = filter.gender?.toEntityCode(),
                initial = filter.initial?.let { "$it".toFilterInitial() },
                query = filter.query.trim().ifBlank { null }
            )
        }

    override fun observeIsEmpty(): Flow<Boolean> =
        nameLocalDataSource.observeTotalCount().map { total -> total == 0 }
}
