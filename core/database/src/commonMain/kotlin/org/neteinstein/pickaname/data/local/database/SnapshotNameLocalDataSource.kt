package org.neteinstein.pickaname.data.local.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Where a [SnapshotNameLocalDataSource] keeps its copy of the list between launches: browser
 * `localStorage` on web, `NSUserDefaults` on iOS. Deliberately a single string rather than a
 * record API - the store always reads and writes the whole list at once (a sync is a
 * purge-and-replace), so anything more granular would be unused complexity.
 */
interface NameSnapshotStorage {
    fun read(): String?
    fun write(value: String)
}

/**
 * The [NameLocalDataSource] for platforms with no SQLite/Room: the whole list is held in memory
 * and mirrored into a [NameSnapshotStorage] so it survives a restart, with every query answered
 * in Kotlin.
 *
 * Viable because of the shape of this data - ~7,500 short strings, a few hundred KB, replaced
 * wholesale by a sync and never written per-row. Android keeps Room (see
 * [RoomNameLocalDataSource]); its SQL is the reference these answers mirror, with two documented
 * differences, both harmless here:
 * - `query` matching uses Kotlin's `ignoreCase`, i.e. full Unicode case folding, where SQLite's
 *   `LIKE` only folds ASCII. Kotlin's is the more useful of the two for Portuguese names.
 * - ordering compares lowercased names rather than SQLite's ASCII-only `COLLATE NOCASE`, which
 *   sorts accented initials after "Z" on both platforms alike.
 */
class SnapshotNameLocalDataSource(private val storage: NameSnapshotStorage) : NameLocalDataSource {

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
        val serialized = storage.read().takeUnless { it.isNullOrEmpty() } ?: return emptyList()
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
        // A full quota (a browser's QuotaExceededError, say) must not take the sync down with
        // it: the in-memory copy above is already live, so the session works normally and only
        // the "survives a restart" part is lost, which the next sync retries anyway.
        runCatching { storage.write(serialized) }
    }

    private fun NameRecord.matches(gender: String?, initial: String?, query: String?): Boolean =
        (gender == null || this.gender == gender) &&
            (initial == null || initialLetter == initial) &&
            (query == null || name.contains(query, ignoreCase = true))

    private fun List<NameRecord>.sortedForDisplay(): List<NameRecord> =
        sortedWith(compareBy({ it.name.lowercase() }, { it.gender }))

    private companion object {
        // Tab/newline-delimited rather than JSON: names come out of a PDF table and can contain
        // neither, so this needs no escaping, no parser and no serialization dependency.
        const val RECORD_SEPARATOR = "\n"
        const val FIELD_SEPARATOR = "\t"
        const val FIELD_COUNT = 3
    }
}
