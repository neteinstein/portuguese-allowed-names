package org.neteinstein.pickaname.domain.repository

import org.neteinstein.pickaname.domain.model.SyncOutcome

/**
 * Downloads the names list PDF from a URL, parses it, and replaces the local database contents.
 * Implementations must purge existing rows before inserting the freshly parsed ones so stale
 * names never linger after a source change.
 */
interface NameSyncRepository {
    suspend fun syncFromUrl(url: String): SyncOutcome
}
