package org.neteinstein.pickaname.data.mapper

import platform.Foundation.NSDiacriticInsensitiveSearch
import platform.Foundation.NSString
import platform.Foundation.stringByFoldingWithOptions

/**
 * Foundation's own diacritic folding - the Apple counterpart of `java.text.Normalizer` on
 * Android and `String.normalize('NFD')` in the browser. Real Unicode data either way, no
 * hand-rolled accent table.
 */
internal actual fun stripDiacritics(value: String): String =
    (value as NSString).stringByFoldingWithOptions(NSDiacriticInsensitiveSearch, locale = null)
