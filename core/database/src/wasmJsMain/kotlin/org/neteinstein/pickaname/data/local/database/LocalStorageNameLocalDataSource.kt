package org.neteinstein.pickaname.data.local.database

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * The web [NameLocalDataSource]: the whole names list is held in memory and mirrored into
 * `localStorage` so it survives a page reload, with every query answered in Kotlin.
 *
 * Why not IndexedDB, which MIGRATION_PLAN.md's risk R1 originally named: the list is a few
 * hundred KB of short strings (one line per name), which fits `localStorage`'s ~5 MB budget
 * with room to spare, and `localStorage` is synchronous - no async plumbing, no schema, no
 * extra dependency, and the store is trivially testable in a real browser. If a future source
 * list ever outgrows that budget, [persist] fails soft (see below) and IndexedDB becomes the
 * upgrade path.
 *
 * Two deliberate differences from the Room/SQLite implementation, both harmless here:
 * - `query` matching uses Kotlin's `ignoreCase`, i.e. full Unicode case folding, where SQLite's
 *   `LIKE` only folds ASCII. Kotlin's is the more useful of the two for Portuguese names.
 * - ordering uses lowercase comparison rather than SQLite's ASCII-only `COLLATE NOCASE`, which
 *   sorts accented initials after "Z" on both platforms alike.
 */
class LocalStorageNameLocalDataSource(
    private val storage: Storage = localStorage
) : NameLocalDataSource {

    private val records = MutableStateFlow(restore())

    override fun observeNames(gender: String?, initial: String?, query: String?): Flow<List<NameRecord>> =
        records.map { all -> all.filter { it.matches(gender, initial, query) }.sortedForDisplay() }

    override fun observeCount(gender: String?, initial: String?, query: String?): Flow<Int> =
        records.map { all -> all.count { it.matches(gender, initial, query) } }

    override fun observeTotalCount(): Flow<Int> = records.map { it.size }

    override fun observeNamesUsedByBothGenders(): Flow<List<String>> =
        records.map { all ->
            all.groupBy { it.name }
                .filterValues { rows -> rows.distinctBy { it.gender }.size > 1 }
                .keys
                .toList()
        }

    override suspend fun replaceAll(names: List<NameRecord>) {
        // Mirrors the Room schema's unique (name, gender) index + OnConflictStrategy.IGNORE,
        // and its autoGenerate ids, which start at 1 for a freshly purged table.
        val deduplicated = names.distinctBy { it.name to it.gender }
            .mapIndexed { index, record -> record.copy(id = index + 1L) }
        records.value = deduplicated
        persist(deduplicated)
    }

    private fun restore(): List<NameRecord> {
        val serialized = storage[STORAGE_KEY].takeUnless { it.isNullOrEmpty() } ?: return emptyList()
        return serialized.split(RECORD_SEPARATOR).mapIndexedNotNull { index, line ->
            val fields = line.split(FIELD_SEPARATOR)
            if (fields.size != FIELD_COUNT) return@mapIndexedNotNull null
            NameRecord(
                id = index + 1L,
                name = fields[0],
                gender = fields[1],
                initialLetter = fields[2]
            )
        }
    }

    private fun persist(records: List<NameRecord>) {
        val serialized = records.joinToString(RECORD_SEPARATOR) {
            "${it.name}$FIELD_SEPARATOR${it.gender}$FIELD_SEPARATOR${it.initialLetter}"
        }
        // A full quota (QuotaExceededError) must not take the sync down with it: the in-memory
        // copy above is already live, so the session works normally and only the "survives a
        // reload" part is lost, which the next sync retries anyway.
        runCatching { storage[STORAGE_KEY] = serialized }
    }

    private fun NameRecord.matches(gender: String?, initial: String?, query: String?): Boolean =
        (gender == null || this.gender == gender) &&
            (initial == null || initialLetter == initial) &&
            (query == null || name.contains(query, ignoreCase = true))

    private fun List<NameRecord>.sortedForDisplay(): List<NameRecord> =
        sortedWith(compareBy({ it.name.lowercase() }, { it.gender }))

    private companion object {
        const val STORAGE_KEY = "pick_a_name.names"

        // Tab/newline-delimited rather than JSON: names come out of a PDF table and can contain
        // neither, so this needs no escaping, no parser and no serialization dependency.
        const val RECORD_SEPARATOR = "\n"
        const val FIELD_SEPARATOR = "\t"
        const val FIELD_COUNT = 3
    }
}
