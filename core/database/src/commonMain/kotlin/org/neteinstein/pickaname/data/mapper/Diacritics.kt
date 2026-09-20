package org.neteinstein.pickaname.data.mapper

/**
 * Unicode NFD-decomposes [value] and drops the combining marks, so "Á" becomes "A".
 *
 * Expect/actual rather than a hand-written character table: both platforms already ship a real
 * Unicode normalizer (`java.text.Normalizer` on Android, `String.prototype.normalize` in the
 * browser), and a table would quietly miss whichever accented letter nobody thought of.
 */
internal expect fun stripDiacritics(value: String): String
