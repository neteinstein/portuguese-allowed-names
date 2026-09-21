package org.neteinstein.pickaname.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.model.TraditionalNameRules
import org.neteinstein.pickaname.domain.repository.NameRepository

/**
 * [NameRepository] over an in-memory list, applying the same gender/initial/query/traditional
 * filtering the real (SQL-backed) one does, so tests exercise filter plumbing rather than a stub
 * that ignores it. [lastFilter] records what the subject actually asked for.
 *
 * One simplification versus the real implementation: `traditionalOnly` here never treats a name
 * as used by both genders, since that cross-reference is a database query rather than filter
 * logic.
 */
class FakeNameRepository(names: List<NameEntry> = emptyList()) : NameRepository {

    private val namesFlow = MutableStateFlow(names)

    var lastFilter: NameFilter? = null
        private set

    fun setNames(names: List<NameEntry>) {
        namesFlow.value = names
    }

    override fun observeNames(filter: NameFilter): Flow<List<NameEntry>> {
        lastFilter = filter
        return namesFlow.map { all -> all.filter { it.matches(filter) } }
    }

    override fun observeNameCount(filter: NameFilter): Flow<Int> {
        lastFilter = filter
        return namesFlow.map { all -> all.count { it.matches(filter) } }
    }

    override fun observeIsEmpty(): Flow<Boolean> = namesFlow.map { it.isEmpty() }

    private fun NameEntry.matches(filter: NameFilter): Boolean {
        val query = filter.query.trim()
        val initial = filter.initial
        return (filter.gender == null || gender == filter.gender) &&
            (initial == null || name.firstOrNull()?.equals(initial, ignoreCase = true) == true) &&
            (query.isEmpty() || name.contains(query, ignoreCase = true)) &&
            (!filter.traditionalOnly || TraditionalNameRules.isTraditional(name))
    }

    companion object {
        /** A couple of entries with both genders represented, for the common case. */
        fun withSampleNames(): FakeNameRepository = FakeNameRepository(
            listOf(
                NameEntry(id = 1, name = "Alice", gender = Gender.FEMALE),
                NameEntry(id = 2, name = "Bruno", gender = Gender.MALE)
            )
        )
    }
}
