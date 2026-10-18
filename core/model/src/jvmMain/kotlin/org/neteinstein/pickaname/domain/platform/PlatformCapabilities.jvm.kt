package org.neteinstein.pickaname.domain.platform

import org.neteinstein.pickaname.domain.model.SearchEngine

actual object PlatformCapabilities {
    /** Native apps download and parse the source themselves, so it is theirs to configure. */
    actual val canConfigureNamesSource: Boolean = true

    actual val searchEngine: SearchEngine = SearchEngine.BRAVE

    actual val privacyPolicyUrl: String = PUBLISHED_PRIVACY_POLICY_URL
}
