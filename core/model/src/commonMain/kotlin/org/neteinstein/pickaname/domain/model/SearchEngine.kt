package org.neteinstein.pickaname.domain.model

import org.neteinstein.pickaname.domain.platform.PlatformCapabilities

/** Which search engine to use when looking up what a name means (long-press on a name). */
enum class SearchEngine {
    GOOGLE,
    DUCKDUCKGO,
    BRAVE;

    companion object {
        /** Used until the user picks one; differs by platform - see [PlatformCapabilities]. */
        val DEFAULT: SearchEngine get() = PlatformCapabilities.defaultSearchEngine
    }
}
