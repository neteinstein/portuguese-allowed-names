@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package org.neteinstein.pickaname.data.parser

import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises the pdf.js-backed extractor in a real browser (`:core:parser:wasmJsTest` →
 * ChromeHeadless) against a hand-built two-column PDF shaped like the real names list: two
 * "<gender> <name>" cells per visual row, laid out side by side.
 *
 * The end-to-end assertion is the one that matters - text out of pdf.js, fed straight into
 * [NameListTextParser], has to produce the same names the Android/pdfbox path would.
 */
class PdfTextExtractorTest {

    private val extractor = PdfTextExtractor()

    @Test
    fun extracts_each_visual_row_as_one_line_in_reading_order() = runTest {
        val text = extractor.extractText(Base64.decode(PdfFixtures.TWO_COLUMN_PDF_BASE64))

        val lines = text.lines().filter { it.isNotBlank() }
        assertEquals(
            listOf("Femininos Ana Masculinos Bruno", "Femininos Beatriz Masculinos Carlos"),
            lines,
            "expected one line per visual row, both columns left to right"
        )
    }

    @Test
    fun extracted_text_parses_into_the_same_names_the_android_path_produces() = runTest {
        val text = extractor.extractText(Base64.decode(PdfFixtures.TWO_COLUMN_PDF_BASE64))

        val parsed = NameListTextParser().parse(text)

        assertEquals(
            listOf("Ana", "Bruno", "Beatriz", "Carlos"),
            parsed.map { it.name }
        )
    }

}
