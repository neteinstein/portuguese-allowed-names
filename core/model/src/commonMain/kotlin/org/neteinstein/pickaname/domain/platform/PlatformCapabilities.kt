package org.neteinstein.pickaname.domain.platform

/**
 * What the app can actually offer on this platform, where the honest answer differs.
 *
 * Kept here rather than in the UI layer because more than one screen has to agree about it:
 * Settings hides the source controls, and the Sync screen hides its "edit source" escape hatch.
 */
expect object PlatformCapabilities {

    /**
     * Whether the user can point the app at a different names source (and choose how often it
     * re-checks).
     *
     * False on web: browsers refuse the cross-origin download the official source would need
     * (MIGRATION_PLAN.md risk R3), so the web build reads a snapshot published alongside it
     * instead. Offering a source URL there would be offering a setting that cannot work.
     */
    val canConfigureNamesSource: Boolean
}
