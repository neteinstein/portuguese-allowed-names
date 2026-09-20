package org.neteinstein.pickaname.presentation.navigation

/**
 * Where a sync run was triggered from - used only to pick a distinct nav-graph route, which is
 * why it lives here rather than in `feature:sync` (a `core` module can't depend on a feature,
 * and the sync screen itself never reads it).
 */
enum class SyncOrigin {
    ONBOARDING,
    SETTINGS
}

/** Central catalogue of nav-graph routes. Plain String routes — no kotlinx-serialization needed. */
object Routes {
    const val SPLASH = "splash"
    const val SYNC_PATTERN = "sync/{origin}"
    const val NAME_LIST = "name_list"
    const val SETTINGS = "settings"

    fun sync(origin: SyncOrigin): String = "sync/${origin.name}"
}
