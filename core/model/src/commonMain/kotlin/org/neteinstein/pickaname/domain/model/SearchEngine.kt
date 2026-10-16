package org.neteinstein.pickaname.domain.model

import org.neteinstein.pickaname.domain.platform.PlatformCapabilities

/** The search engine used to look up what a name means (tap or long-press on a name). */
enum class SearchEngine {
    GOOGLE,
    BRAVE;

    companion object {
        /** Fixed per platform, not user-configurable - see [PlatformCapabilities.searchEngine]. */
        val CURRENT: SearchEngine get() = PlatformCapabilities.searchEngine
    }
}
