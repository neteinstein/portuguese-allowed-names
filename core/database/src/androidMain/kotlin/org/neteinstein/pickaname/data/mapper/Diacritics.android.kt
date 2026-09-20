package org.neteinstein.pickaname.data.mapper

import java.text.Normalizer

private val COMBINING_MARKS = Regex("\\p{M}")

internal actual fun stripDiacritics(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD).replace(COMBINING_MARKS, "")
