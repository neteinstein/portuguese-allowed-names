package org.neteinstein.pickaname.domain.platform

import org.neteinstein.pickaname.domain.model.SearchEngine

actual object PlatformCapabilities {
    /** Native apps download and parse the source themselves, so it is theirs to configure. */
    actual val canConfigureNamesSource: Boolean = true

    /** Shown in the in-app bottom sheet, where Brave renders cleanly embedded. */
    actual val searchEngine: SearchEngine = SearchEngine.BRAVE
}
