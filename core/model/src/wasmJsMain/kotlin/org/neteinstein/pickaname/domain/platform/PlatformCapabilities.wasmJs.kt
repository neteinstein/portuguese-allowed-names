package org.neteinstein.pickaname.domain.platform

import org.neteinstein.pickaname.domain.model.SearchEngine

actual object PlatformCapabilities {
    /**
     * False: the web build syncs from the snapshot CI publishes next to it, because the official
     * source cannot be fetched from a browser at all (no CORS header - see risk R3). A source
     * URL setting here would do nothing.
     */
    actual val canConfigureNamesSource: Boolean = false

    /** Google: the search opens in a new browser tab here, so embedding is irrelevant. */
    actual val searchEngine: SearchEngine = SearchEngine.GOOGLE

    actual val privacyPolicyUrl: String = "privacy.html"
}
