package org.neteinstein.pickaname.domain.model

/**
 * Result of a names-source synchronisation (download + parse + persist).
 */
sealed interface SyncOutcome {
    data class Success(val namesLoaded: Int) : SyncOutcome
    data class Error(val reason: SyncFailureReason, val message: String? = null) : SyncOutcome
}

/**
 * Coarse-grained classification of why a sync failed, so the UI can offer the right recovery
 * action (retry vs. edit the source URL).
 */
enum class SyncFailureReason {
    NETWORK,
    INVALID_SOURCE,
    NO_NAMES_FOUND,
    UNKNOWN
}
