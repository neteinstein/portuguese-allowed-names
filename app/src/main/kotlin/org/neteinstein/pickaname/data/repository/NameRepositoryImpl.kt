package org.neteinstein.pickaname.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.neteinstein.pickaname.data.local.database.NameDao
import org.neteinstein.pickaname.data.mapper.toDomain
import org.neteinstein.pickaname.data.mapper.toEntityCode
import org.neteinstein.pickaname.data.mapper.toFilterInitial
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.model.TraditionalNameRules
import org.neteinstein.pickaname.domain.repository.NameRepository

class NameRepositoryImpl(
    private val nameDao: NameDao
) : NameRepository {

    override fun observeNames(filter: NameFilter): Flow<List<NameEntry>> =
        nameDao.observeNames(
            gender = filter.gender?.toEntityCode(),
            initial = filter.initial?.let { "$it".toFilterInitial() },
            query = filter.query.trim().ifBlank { null }
        ).map { entities -> entities.map { it.toDomain() }.applyTraditionalOnly(filter.traditionalOnly) }

    // TraditionalNameRules isn't expressible as SQL, so when it's active the count is derived
    // from the same in-memory filtering as observeNames rather than from a DB COUNT(*) query.
    override fun observeNameCount(filter: NameFilter): Flow<Int> =
        if (filter.traditionalOnly) {
            observeNames(filter).map { it.size }
        } else {
            nameDao.observeCount(
                gender = filter.gender?.toEntityCode(),
                initial = filter.initial?.let { "$it".toFilterInitial() },
                query = filter.query.trim().ifBlank { null }
            )
        }

    override fun observeIsEmpty(): Flow<Boolean> =
        nameDao.observeTotalCount().map { total -> total == 0 }
}

private fun List<NameEntry>.applyTraditionalOnly(enabled: Boolean): List<NameEntry> =
    if (enabled) filter { TraditionalNameRules.isTraditional(it.name) } else this
