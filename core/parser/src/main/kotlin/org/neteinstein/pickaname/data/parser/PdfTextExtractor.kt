package org.neteinstein.pickaname.data.parser

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thin wrapper around pdfbox-android responsible only for turning PDF bytes into plain text,
 * one line per visual row. Kept separate from [NameListTextParser] so the actual names-list
 * parsing logic stays pure Kotlin and unit-testable without an Android/PDF runtime.
 *
 * Requires `PDFBoxResourceLoader.init(context)` to have been called once (done in
 * `PickANameApplication`) before this is used.
 */
class PdfTextExtractor {

    suspend fun extractText(pdfBytes: ByteArray): String = withContext(Dispatchers.Default) {
        PDDocument.load(pdfBytes).use { document ->
            val stripper = PDFTextStripper()
            stripper.setSortByPosition(true)
            stripper.setLineSeparator("\n")
            stripper.getText(document)
        }
    }
}

