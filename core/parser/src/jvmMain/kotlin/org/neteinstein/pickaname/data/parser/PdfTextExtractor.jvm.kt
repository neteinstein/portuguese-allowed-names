package org.neteinstein.pickaname.data.parser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

/**
 * JVM [PdfTextExtractor] actual: Apache PDFBox, configured exactly like the Android actual's
 * pdfbox-android fork (same version line, `sortByPosition`, `\n` line separator) so the text it
 * produces is the text the app would see on a phone.
 *
 * This target exists for the snapshot generator that runs in CI, not for an app.
 */
actual class PdfTextExtractor actual constructor() {

    actual suspend fun extractText(pdfBytes: ByteArray): String = withContext(Dispatchers.Default) {
        PDDocument.load(pdfBytes).use { document ->
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true
            stripper.lineSeparator = "\n"
            stripper.getText(document)
        }
    }
}
