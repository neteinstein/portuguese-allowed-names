package org.neteinstein.pickaname.domain.model

/**
 * Outcome of the periodic auto-refresh check performed whenever the app opens straight to the
 * name list (i.e. no initial sync was needed). The database is only purged and repopulated when
 * this resolves to [Refreshed] - see [org.neteinstein.pickaname.domain.repository.NameSyncRepository].
 */
sealed interface AutoRefreshResult {

    /** The configured [RefreshPeriod] has not elapsed yet; nothing was done. */
    data object NotDue : AutoRefreshResult

    /** The refresh was due and completed successfully; the database now holds fresh data. */
    data class Refreshed(val namesLoaded: Int) : AutoRefreshResult

    /**
     * The refresh was due but failed; the existing database is left untouched and the next app
     * open will retry.
     */
    data class Failed(val reason: SyncFailureReason) : AutoRefreshResult
}
