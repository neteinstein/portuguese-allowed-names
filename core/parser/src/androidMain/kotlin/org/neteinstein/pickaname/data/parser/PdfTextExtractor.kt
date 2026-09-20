package org.neteinstein.pickaname.data.parser

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android [PdfTextExtractor] actual: thin wrapper around pdfbox-android.
 *
 * Requires `PDFBoxResourceLoader.init(context)` to have been called once (done in
 * `PickANameApplication`) before this is used.
 */
actual class PdfTextExtractor actual constructor() {

    actual suspend fun extractText(pdfBytes: ByteArray): String = withContext(Dispatchers.Default) {
        PDDocument.load(pdfBytes).use { document ->
            val stripper = PDFTextStripper()
            stripper.setSortByPosition(true)
            stripper.setLineSeparator("\n")
            stripper.getText(document)
        }
    }
}

