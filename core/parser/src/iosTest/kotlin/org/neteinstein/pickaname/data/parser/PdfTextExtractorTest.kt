@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package org.neteinstein.pickaname.data.parser

import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The iOS half of what `PdfTextExtractorTest` asserts for web: the same fixture, the same
 * expected rows, through a completely different PDF engine (PDFKit rather than pdf.js).
 *
 * This test exists because the first PDFKit implementation passed everything *except* reality:
 * `PDFDocument.string` returned the document one fragment per line inside a running app while
 * returning whole rows in a test process, so the app parsed 488 names instead of 7,481. The
 * extractor now rebuilds rows from fragment positions, and this pins the shape it must produce.
 */
class PdfTextExtractorTest {

    private val extractor = PdfTextExtractor()

    @Test
    fun extracts_each_visual_row_as_one_line_in_reading_order() = runTest {
        val text = extractor.extractText(Base64.decode(PdfFixtures.TWO_COLUMN_PDF_BASE64))

        assertEquals(
            listOf("Femininos Ana Masculinos Bruno", "Femininos Beatriz Masculinos Carlos"),
            text.lines().filter { it.isNotBlank() },
            "expected one line per visual row, both columns left to right"
        )
    }

    @Test
    fun extracted_text_parses_into_the_same_names_the_other_platforms_produce() = runTest {
        val text = extractor.extractText(Base64.decode(PdfFixtures.TWO_COLUMN_PDF_BASE64))

        assertEquals(
            listOf("Ana", "Bruno", "Beatriz", "Carlos"),
            NameListTextParser().parse(text).map { it.name }
        )
    }
}
