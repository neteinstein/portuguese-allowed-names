package org.neteinstein.pickaname.domain.platform

actual object PlatformCapabilities {
    /** Native apps download and parse the source themselves, so it is theirs to configure. */
    actual val canConfigureNamesSource: Boolean = true
}
