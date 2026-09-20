package org.neteinstein.pickaname.data.parser

import org.neteinstein.pickaname.domain.model.Gender
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers the one thing the two platforms' PDF extractors genuinely disagree about: how much
 * whitespace lands between a row's two columns.
 *
 * pdfbox (Android) emits a double space there; pdf.js (web) can't tell that gap apart from the
 * one between a gender keyword and its name - measured on the real source document, the two
 * overlap - so it emits single spaces throughout and relies on the gender keyword itself to mark
 * where a cell starts. Both shapes have to parse to the same names, which is what this asserts.
 *
 * Written against `kotlin.test` so it runs on wasmJs as well as Android (the older
 * `NameListTextParserTest` stays on JUnit/Truth in `androidUnitTest` until Phase 5).
 */
class NameListTextParserCellSplitTest {

    private val parser = NameListTextParser()

    @Test
    fun parses_double_space_separated_columns_the_android_extractor_produces() {
        val parsed = parser.parse(
            """
            Femininos Aabirah  Masculinos Aabaj
            Femininos Aaditi  Masculinos Aagambir
            """.trimIndent()
        )

        assertEquals(
            listOf(
                ParsedName("Aabirah", Gender.FEMALE),
                ParsedName("Aabaj", Gender.MALE),
                ParsedName("Aaditi", Gender.FEMALE),
                ParsedName("Aagambir", Gender.MALE)
            ),
            parsed
        )
    }

    @Test
    fun parses_single_space_separated_columns_the_web_extractor_produces() {
        val parsed = parser.parse(
            """
            Femininos Aabirah Masculinos Aabaj
            Femininos Aaditi Masculinos Aagambir
            """.trimIndent()
        )

        assertEquals(
            listOf(
                ParsedName("Aabirah", Gender.FEMALE),
                ParsedName("Aabaj", Gender.MALE),
                ParsedName("Aaditi", Gender.FEMALE),
                ParsedName("Aagambir", Gender.MALE)
            ),
            parsed
        )
    }

    @Test
    fun keeps_multi_word_names_in_one_cell_when_columns_are_single_spaced() {
        val parsed = parser.parse("Femininos Maria Madalena Masculinos João Pedro")

        assertEquals(
            listOf(
                ParsedName("Maria Madalena", Gender.FEMALE),
                ParsedName("João Pedro", Gender.MALE)
            ),
            parsed
        )
    }

    @Test
    fun still_stitches_a_name_that_wraps_onto_the_next_line() {
        val parsed = parser.parse(
            """
            Femininos Celtiane Masculinos Darius-
            Alexandru
            """.trimIndent()
        )

        assertEquals(
            listOf(
                ParsedName("Celtiane", Gender.FEMALE),
                ParsedName("Darius-Alexandru", Gender.MALE)
            ),
            parsed
        )
    }
}
