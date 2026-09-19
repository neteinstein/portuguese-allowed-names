package org.neteinstein.pickaname.domain.repository

import kotlinx.coroutines.flow.Flow
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter

/**
 * Read access to the locally persisted names list. Writes only happen as part of a full
 * purge-and-replace sync, see [NameSyncRepository].
 */
interface NameRepository {

    /** Emits the filtered, sorted list of names, updating whenever the underlying data changes. */
    fun observeNames(filter: NameFilter): Flow<List<NameEntry>>

    /** Emits how many names currently match [filter]. */
    fun observeNameCount(filter: NameFilter): Flow<Int>

    /** Emits `true` while the local names table has no rows at all (e.g. before first sync). */
    fun observeIsEmpty(): Flow<Boolean>
}
