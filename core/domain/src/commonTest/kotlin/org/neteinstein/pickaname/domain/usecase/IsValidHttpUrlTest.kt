package org.neteinstein.pickaname.domain.usecase

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the hand-rolled replacement for the `java.net.URI` check [UpdateSourceUrlUseCase] used
 * to do - same accept/reject answers as before, but on every target. Written against
 * `kotlin.test` (not JUnit/Truth/MockK like the older use-case tests) so it runs on wasmJs too.
 */
class IsValidHttpUrlTest {

    @Test
    fun accepts_https_and_http_urls_with_a_host() {
        assertTrue(isValidHttpUrl("https://example.com/list.pdf"))
        assertTrue(isValidHttpUrl("http://example.com/list.pdf"))
        assertTrue(isValidHttpUrl("HTTPS://example.com"))
        assertTrue(isValidHttpUrl("https://example.com:8443/a?b=c#d"))
        assertTrue(isValidHttpUrl("https://user:pass@example.com/list.pdf"))
    }

    @Test
    fun rejects_blank_input() {
        assertFalse(isValidHttpUrl(""))
        assertFalse(isValidHttpUrl("   "))
    }

    @Test
    fun rejects_unsupported_schemes_and_missing_schemes() {
        assertFalse(isValidHttpUrl("ftp://example.com/list.pdf"))
        assertFalse(isValidHttpUrl("file:///tmp/list.pdf"))
        assertFalse(isValidHttpUrl("example.com/list.pdf"))
        assertFalse(isValidHttpUrl("://example.com"))
    }

    @Test
    fun rejects_urls_without_a_host() {
        assertFalse(isValidHttpUrl("https://"))
        assertFalse(isValidHttpUrl("https:///list.pdf"))
        assertFalse(isValidHttpUrl("https://:8443/list.pdf"))
    }

    @Test
    fun rejects_malformed_input_with_whitespace() {
        assertFalse(isValidHttpUrl("not a url at all"))
        assertFalse(isValidHttpUrl("https://exa mple.com"))
    }
}
