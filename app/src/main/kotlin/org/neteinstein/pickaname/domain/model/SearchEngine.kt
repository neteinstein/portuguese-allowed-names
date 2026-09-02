package org.neteinstein.pickaname.domain.model

/** Which search engine to use when looking up what a name means (long-press on a name). */
enum class SearchEngine {
    GOOGLE,
    DUCKDUCKGO;

    companion object {
        val DEFAULT: SearchEngine = DUCKDUCKGO
    }
}
