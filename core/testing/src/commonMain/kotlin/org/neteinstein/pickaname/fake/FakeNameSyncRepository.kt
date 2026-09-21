package org.neteinstein.pickaname.fake

import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.repository.NameSyncRepository

/**
 * Returns queued [SyncOutcome]s in order (repeating the last one once exhausted) and records the
 * URLs it was asked to sync, which is what the retry-after-failure tests assert on.
 */
class FakeNameSyncRepository(
    private vararg val outcomes: SyncOutcome
) : NameSyncRepository {

    val syncedUrls = mutableListOf<String>()

    override suspend fun syncFromUrl(url: String): SyncOutcome {
        val outcome = outcomes.getOrElse(syncedUrls.size) {
            outcomes.lastOrNull() ?: SyncOutcome.Success(namesLoaded = 0)
        }
        syncedUrls += url
        return outcome
    }
}
