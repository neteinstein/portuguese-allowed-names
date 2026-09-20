package org.neteinstein.pickaname.data.parser

/**
 * wasmJs [PdfTextExtractor] actual - **not implemented yet**.
 *
 * The real implementation needs `pdf.js` JS interop (risk R2 in MIGRATION_PLAN.md): `external`
 * declarations against `pdfjsLib`, a `<script>` tag for it in `webApp/index.html`, and bridging
 * its Promise-based API into a suspend function. That interop can't be meaningfully written or
 * verified in this sandbox - there's no browser here to run `wasmJsBrowserTest` against, so
 * blind interop code would be unverified guesswork rather than a real implementation. Structural
 * scaffolding (this module compiling for wasmJs at all) is in place; this stub makes the gap
 * explicit instead of silently pretending web sync works.
 */
actual class PdfTextExtractor actual constructor() {

    actual suspend fun extractText(pdfBytes: ByteArray): String {
        throw NotImplementedError(
            "PdfTextExtractor has no wasmJs implementation yet - see MIGRATION_PLAN.md risk R2."
        )
    }
}
