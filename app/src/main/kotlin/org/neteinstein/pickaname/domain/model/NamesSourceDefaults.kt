package org.neteinstein.pickaname.domain.model

/**
 * Default configuration for where the official names list is downloaded from.
 * The user can override this from Settings; [DEFAULT_SOURCE_URL] is what ships out of the box
 * and what "Reset to default" restores.
 */
object NamesSourceDefaults {
    const val DEFAULT_SOURCE_URL: String =
        "https://link.neteinstein.org/portuguese-allowed-names-composition"
}
