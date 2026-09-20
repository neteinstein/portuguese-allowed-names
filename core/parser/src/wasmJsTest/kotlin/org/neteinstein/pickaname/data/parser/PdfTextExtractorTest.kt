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
        val text = extractor.extractText(Base64.decode(TWO_COLUMN_PDF_BASE64))

        val lines = text.lines().filter { it.isNotBlank() }
        assertEquals(
            listOf("Femininos Ana Masculinos Bruno", "Femininos Beatriz Masculinos Carlos"),
            lines,
            "expected one line per visual row, both columns left to right"
        )
    }

    @Test
    fun extracted_text_parses_into_the_same_names_the_android_path_produces() = runTest {
        val text = extractor.extractText(Base64.decode(TWO_COLUMN_PDF_BASE64))

        val parsed = NameListTextParser().parse(text)

        assertEquals(
            listOf("Ana", "Bruno", "Beatriz", "Carlos"),
            parsed.map { it.name }
        )
    }

    private companion object {
        /**
         * A 2x2 "GÉNERO NOME" table, built by hand (see MIGRATION_PLAN.md Phase 3) rather than
         * committing a slice of the real 2.9 MB source PDF: inline so the test needs no resource
         * loading, and small enough to stay readable in review.
         */
        const val TWO_COLUMN_PDF_BASE64 =
            "JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCjIgMCBvYmoKPD" +
            "wgL1R5cGUgL1BhZ2VzIC9LaWRzIFszIDAgUl0gL0NvdW50IDEgPj4KZW5kb2JqCjMgMCBvYmoKPDwgL1R5cGUgL1Bh" +
            "Z2UgL1BhcmVudCAyIDAgUiAvTWVkaWFCb3ggWzAgMCA0MjAgMjAwXSAvQ29udGVudHMgNCAwIFIgL1Jlc291cmNlcy" +
            "A8PCAvRm9udCA8PCAvRjEgNSAwIFIgPj4gPj4gPj4KZW5kb2JqCjQgMCBvYmoKPDwgL0xlbmd0aCAxNDIgPj4Kc3Ry" +
            "ZWFtCkJUCi9GMSAxMSBUZgo0MCAxNjAgVGQgKEZlbWluaW5vcyBBbmEpIFRqCjIwMCAwIFRkIChNYXNjdWxpbm9zIE" +
            "JydW5vKSBUagotMjAwIC0yMCBUZCAoRmVtaW5pbm9zIEJlYXRyaXopIFRqCjIwMCAwIFRkIChNYXNjdWxpbm9zIENh" +
            "cmxvcykgVGoKRVQKZW5kc3RyZWFtCmVuZG9iago1IDAgb2JqCjw8IC9UeXBlIC9Gb250IC9TdWJ0eXBlIC9UeXBlMS" +
            "AvQmFzZUZvbnQgL0hlbHZldGljYSA+PgplbmRvYmoKeHJlZgowIDYKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAw" +
            "MDA5IDAwMDAwIG4gCjAwMDAwMDAwNTggMDAwMDAgbiAKMDAwMDAwMDExNSAwMDAwMCBuIAowMDAwMDAwMjQxIDAwMD" +
            "AwIG4gCjAwMDAwMDA0MzQgMDAwMDAgbiAKdHJhaWxlcgo8PCAvU2l6ZSA2IC9Sb290IDEgMCBSID4+CnN0YXJ0eHJl" +
            "Zgo1MDQKJSVFT0YK"
    }
}
