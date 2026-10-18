package org.neteinstein.pickaname.domain.platform

import org.neteinstein.pickaname.domain.model.SearchEngine

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

    /**
     * The search engine used to look up a name's meaning. Not user-configurable.
     *
     * Brave where the search is shown inside the app's own bottom sheet; Google on web, where
     * search engines refuse to be embedded and the search always opens in a new browser tab.
     */
    val searchEngine: SearchEngine

    /**
     * Where Settings' "privacy policy" button points: the page generated from PRIVACY_POLICY.md
     * (tools/privacy-page) and published with the web build.
     *
     * Relative on web, so it opens the copy deployed next to the page being viewed; absolute
     * everywhere else.
     */
    val privacyPolicyUrl: String
}

/** The published privacy policy, for the platforms that aren't the website itself. */
const val PUBLISHED_PRIVACY_POLICY_URL = "https://nomespermitidosemportugal.neteinstein.org/privacy.html"
