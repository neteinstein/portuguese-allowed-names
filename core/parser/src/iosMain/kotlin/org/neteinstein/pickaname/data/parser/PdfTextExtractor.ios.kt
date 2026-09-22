@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package org.neteinstein.pickaname.data.parser

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFPage
import platform.PDFKit.PDFSelection
import platform.PDFKit.kPDFDisplayBoxMediaBox
import kotlin.math.abs
import kotlin.math.max

/**
 * iOS [PdfTextExtractor] actual: PDFKit, which ships with the platform - the counterpart of
 * pdfbox-android on Android and pdf.js on web, and the only one of the three needing no
 * third-party dependency.
 *
 * It rebuilds the document's rows from **positioned fragments**, the same way the web extractor
 * does with pdf.js, rather than trusting PDFKit's own text layout. That is not a stylistic
 * choice: in a running app, `PDFDocument.string`, `PDFPage.string` and `selectionsByLine()` all
 * returned the page's text one fragment per line ("Masculinos", "Femininos", "Abd", ...) where
 * the same code in a test process returned whole rows - identical characters, different
 * arrangement, and 488 names parsed instead of 7,481. Grouping fragments by baseline is the one
 * approach that asks PDFKit only for facts it reports consistently: what the text is, and where
 * it sits.
 */
actual class PdfTextExtractor actual constructor() {

    actual suspend fun extractText(pdfBytes: ByteArray): String {
        val document = PDFDocument(data = pdfBytes.toNSData())
            ?: error("Could not read the downloaded file as a PDF")

        return buildString {
            for (pageIndex in 0 until document.pageCount.toInt()) {
                val page = document.pageAtIndex(pageIndex.toULong()) ?: continue
                for (line in page.rowsByBaseline()) {
                    append(line)
                    append('\n')
                }
            }
        }
    }
}

private class Fragment(val x: Double, val y: Double, val height: Double, val text: String)

/** Every visual row on this page, top to bottom, each rebuilt left to right. */
private fun PDFPage.rowsByBaseline(): List<String> {
    val wholePage = selectionForRect(boundsForBox(kPDFDisplayBoxMediaBox)) ?: return emptyList()

    @Suppress("UNCHECKED_CAST")
    val fragments = (wholePage.selectionsByLine() as List<PDFSelection>).mapNotNull { selection ->
        val text = selection.string?.trim().orEmpty()
        if (text.isEmpty()) return@mapNotNull null
        selection.boundsForPage(this).useContents {
            Fragment(x = origin.x, y = origin.y, height = size.height, text = text)
        }
    }
    if (fragments.isEmpty()) return emptyList()

    // Rows are grown from the topmost baseline rather than bucketed by rounded y, so a fragment
    // sitting slightly off its row's baseline still joins it (the same reasoning as the web
    // extractor, where one wrapped hyphenated name depends on it).
    val rows = mutableListOf<MutableList<Fragment>>()
    var anchor: Fragment? = null
    for (fragment in fragments.sortedByDescending { it.y }) {
        val tolerance = max(MIN_LINE_TOLERANCE, LINE_TOLERANCE_RATIO * fragment.height)
        if (anchor == null || abs(anchor.y - fragment.y) > tolerance) {
            anchor = fragment
            rows += mutableListOf(fragment)
        } else {
            rows.last() += fragment
        }
    }

    return rows.map { row ->
        row.sortedBy { it.x }.joinToString(" ") { it.text }
    }
}

// addressOf(0) has nothing to point at for an empty array, so that case short-circuits (an
// empty download is not a PDF anyway - the caller reports it as an invalid source).
private fun ByteArray.toNSData(): NSData =
    if (isEmpty()) NSData() else usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

private const val LINE_TOLERANCE_RATIO = 0.7
private const val MIN_LINE_TOLERANCE = 2.0
