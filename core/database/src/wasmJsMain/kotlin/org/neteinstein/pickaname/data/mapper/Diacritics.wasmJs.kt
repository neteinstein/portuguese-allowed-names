@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.neteinstein.pickaname.data.mapper

/**
 * `String.prototype.normalize("NFD")` plus a combining-marks strip - the browser's own Unicode
 * data, the direct counterpart of `java.text.Normalizer` on Android. `\p{M}` needs the `u` flag
 * in JS regex, hence the explicit `RegExp` construction rather than a Kotlin [Regex].
 */
private fun normalizeAndStrip(value: String): String =
    js("value.normalize('NFD').replace(/\\p{M}/gu, '')")

internal actual fun stripDiacritics(value: String): String = normalizeAndStrip(value)
