@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.neteinstein.pickaname.data.parser

import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * wasmJs [PdfTextExtractor] actual: pdf.js (`pdfjs-dist`, an npm dependency of this module's
 * wasmJs target, bundled by webpack) in place of Android's pdfbox.
 *
 * pdf.js hands back positioned text fragments rather than lines, so [extractTextFromPdf]
 * reassembles them: group by baseline, order by x, and join with a single space wherever the
 * fragments don't already touch. It deliberately does *not* try to reproduce the double-space
 * column separator pdfbox emits - measured against the real source document, the gender→name gap
 * within a cell (~73-80pt) and the name→next-column gap (~89pt, but far smaller after a long
 * name) overlap, so no gap threshold can tell them apart. [NameListTextParser] splits cells on
 * gender keywords as well as on double spaces precisely so this side doesn't have to guess.
 */
actual class PdfTextExtractor actual constructor() {

    actual suspend fun extractText(pdfBytes: ByteArray): String =
        extractTextFromPdf(pdfBytes.toUint8Array()).await<JsString>().toString()
}

/** Copies the PDF bytes into a `Uint8Array`, which is what pdf.js's `getDocument` wants. */
private fun ByteArray.toUint8Array(): JsAny {
    val array = newUint8Array(size)
    forEachIndexed { index, byte -> setByte(array, index, byte.toInt()) }
    return array
}

private fun newUint8Array(size: Int): JsAny = js("new Uint8Array(size)")

private fun setByte(array: JsAny, index: Int, value: Int) {
    js("array[index] = value & 0xFF")
}

/**
 * Runs the whole extraction in one JS coroutine, rather than crossing the Kotlin/JS boundary per
 * page or per fragment - the source document is 88 pages of table rows, so per-fragment interop
 * calls would dominate the runtime.
 */
private fun extractTextFromPdf(data: JsAny): Promise<JsString> = js(
    """(async () => {
        const pdfjs = await import('pdfjs-dist/build/pdf.min.mjs');
        // Run the worker module in the page instead of a real Web Worker: Kotlin/Wasm's webpack
        // output is a classic script, so neither `import.meta.url` nor `new Worker(new URL(...))`
        // - the two documented ways to point GlobalWorkerOptions at the worker bundle - are
        // available here. pdf.js's own fake-worker path picks this up.
        globalThis.pdfjsWorker = await import('pdfjs-dist/build/pdf.worker.min.mjs');

        const doc = await pdfjs.getDocument({ data: data, isEvalSupported: false }).promise;
        const WORD_GAP = 1.0;             // pt: wider than this between fragments means a space
        const LINE_TOLERANCE_RATIO = 0.7; // of text height: how far off a baseline still counts
        const MIN_LINE_TOLERANCE = 2.0;   // pt: floor for tiny text

        const lines = [];
        for (let pageNumber = 1; pageNumber <= doc.numPages; pageNumber++) {
            const page = await doc.getPage(pageNumber);
            const content = await page.getTextContent();

            const fragments = [];
            for (const item of content.items) {
                // pdf.js also emits synthetic whitespace-only items to mark gaps; positions below
                // say everything those would, and keeping them would double-space arbitrarily.
                if (!item.str || item.str.trim().length === 0) continue;
                fragments.push({
                    x: item.transform[4],
                    y: item.transform[5],
                    width: item.width || 0,
                    height: item.height || 0,
                    text: item.str
                });
            }
            fragments.sort((a, b) => b.y - a.y);

            // Rows are grown greedily from the topmost baseline rather than bucketed by rounded
            // y: a cell whose contents wrap (the document's one hyphenated name does) sits a few
            // points off its row's baseline, and pdfbox folds that back into the row it belongs
            // to. Bucketing would scatter it into a row of its own and lose the name.
            const rows = [];
            let current = null;
            for (const fragment of fragments) {
                const tolerance = Math.max(MIN_LINE_TOLERANCE, LINE_TOLERANCE_RATIO * fragment.height);
                if (current === null || Math.abs(current.y - fragment.y) > tolerance) {
                    current = { y: fragment.y, fragments: [] };
                    rows.push(current);
                }
                current.fragments.push(fragment);
            }

            for (const row of rows) {
                row.fragments.sort((a, b) => a.x - b.x);
                let line = '';
                let cursor = null;
                for (const fragment of row.fragments) {
                    // A word the PDF splits across fragments ("Mascul" + "inos") has no gap at
                    // all; anything wider is a space.
                    if (cursor !== null && fragment.x - cursor > WORD_GAP && !line.endsWith(' ')) {
                        line += ' ';
                    }
                    line += fragment.text;
                    cursor = fragment.x + fragment.width;
                }
                const trimmed = line.trim();
                if (trimmed.length > 0) lines.push(trimmed);
            }
            page.cleanup();
        }
        await doc.destroy();
        return lines.join('\n');
    })()"""
)
