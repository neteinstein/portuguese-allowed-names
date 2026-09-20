package org.neteinstein.pickaname.data.local.database

import kotlinx.coroutines.flow.Flow

/**
 * A single stored (name, gender) pair, platform-neutral.
 *
 * The Android store persists these as Room [NameEntity] rows; the web store keeps them in
 * `localStorage`. [initialLetter] is precomputed at write time (accent-stripped, upper-cased
 * first character of [name], see `String.toFilterInitial()`) purely as a fast filter key.
 */
data class NameRecord(
    val id: Long = 0,
    val name: String,
    val gender: String,
    val initialLetter: String
)

/**
 * The local names store, as the rest of the app sees it - deliberately expressed in plain
 * Kotlin rather than as a Room DAO, because Room has no wasmJs support at all (risk R1 in
 * MIGRATION_PLAN.md): `room-runtime` publishes no `wasm-js` artifact, only `room-common`'s
 * annotations do.
 *
 * Android backs this with Room (`RoomNameLocalDataSource`), web with `localStorage`
 * (`LocalStorageNameLocalDataSource`). Both must answer the queries below identically; the
 * Room implementation's SQL is the reference, and the web implementation documents where
 * SQLite's semantics (ASCII-only `LIKE`/`NOCASE` folding) differ from Kotlin's.
 */
interface NameLocalDataSource {

    /**
     * Names matching every non-null criterion, ordered case-insensitively by name then gender.
     * A null [gender]/[initial] means "don't filter on it"; [query] is a case-insensitive
     * substring match and is expected to be blank-normalized to null by the caller.
     */
    fun observeNames(gender: String?, initial: String?, query: String?): Flow<List<NameRecord>>

    /** How many records [observeNames] would return for the same arguments. */
    fun observeCount(gender: String?, initial: String?, query: String?): Flow<Int>

    /** Total stored record count, unfiltered - drives "has the initial sync happened yet?". */
    fun observeTotalCount(): Flow<Int>

    /** Names stored under more than one gender - used to flag likely-unisex names. */
    fun observeNamesUsedByBothGenders(): Flow<List<String>>

    /**
     * Purges the store and repopulates it with [names] atomically, ignoring duplicate
     * (name, gender) pairs. Ids are assigned by the store, so the caller passes records with
     * the default id.
     */
    suspend fun replaceAll(names: List<NameRecord>)
}
