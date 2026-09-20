package org.neteinstein.pickaname.data.parser

/**
 * Turns PDF bytes into plain text, one line per visual row. Kept separate from
 * [NameListTextParser] so the actual names-list parsing logic stays pure Kotlin and
 * unit-testable, independent of whatever platform-specific PDF runtime produces its input.
 *
 * `actual` per platform (see MIGRATION_PLAN.md's Phase 3 notes for the wasmJs actual's current
 * status - it's a stub, not a real implementation yet):
 * - Android: wraps `pdfbox-android` (unchanged behavior from before this module went
 *   multiplatform).
 * - wasmJs: intended to wrap `pdf.js` via JS interop (risk R2 in the migration plan) - not
 *   implemented yet.
 */
expect class PdfTextExtractor() {
    suspend fun extractText(pdfBytes: ByteArray): String
}
