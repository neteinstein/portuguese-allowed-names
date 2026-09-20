package org.neteinstein.pickaname.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The browser-side half of what `NameRecordMappersTest` asserts on Android - same inputs, same
 * expected answers, so the two `stripDiacritics` actuals can't quietly drift apart.
 */
class DiacriticsWasmJsTest {

    @Test
    fun toFilterInitial_strips_diacritics_and_upper_cases() {
        assertEquals("A", "Áddison".toFilterInitial())
        assertEquals("A", "ãdi".toFilterInitial())
        assertEquals("Z", "Zoé".toFilterInitial())
        assertEquals("K", "kevin".toFilterInitial())
        assertEquals("C", "Çesar".toFilterInitial())
    }

    @Test
    fun toFilterInitial_returns_empty_string_for_empty_input() {
        assertEquals("", "".toFilterInitial())
    }
}
