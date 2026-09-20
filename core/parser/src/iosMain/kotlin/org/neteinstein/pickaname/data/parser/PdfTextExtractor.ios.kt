@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package org.neteinstein.pickaname.data.parser

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.PDFKit.PDFDocument

/**
 * iOS [PdfTextExtractor] actual: PDFKit, which ships with the platform - the counterpart of
 * pdfbox-android on Android and pdf.js on web, and the only one of the three that needs no
 * third-party dependency at all.
 *
 * `PDFDocument.string` returns the document's text with its own line breaks, which is the same
 * shape pdfbox produces; [NameListTextParser] splits cells on gender keywords as well as on
 * runs of spaces (see its docs), so it copes with either extractor's spacing.
 */
actual class PdfTextExtractor actual constructor() {

    actual suspend fun extractText(pdfBytes: ByteArray): String {
        val document = PDFDocument(data = pdfBytes.toNSData())
            ?: error("Could not read the downloaded file as a PDF")
        return document.string() ?: ""
    }
}

// addressOf(0) has nothing to point at for an empty array, so that case short-circuits (an
// empty download is not a PDF anyway - the caller reports it as an invalid source).
private fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData() else usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
