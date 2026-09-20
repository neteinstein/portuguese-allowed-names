@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.neteinstein.pickaname.presentation.namelist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.browser.window

@Composable
actual fun rememberExternalUrlOpener(): (String) -> Unit = { url ->
    // "_blank" + noopener: a search engine tab shouldn't get a handle back to this page.
    window.open(url, "_blank", "noopener")
}

/**
 * Always false on web: search engines refuse to be framed (`X-Frame-Options` /
 * `frame-ancestors`), so an in-page browser could only ever show a blank panel. The name-meaning
 * search opens in a new tab here instead, via [rememberExternalUrlOpener].
 */
@Composable
actual fun isInAppBrowserSupported(): Boolean = false

/** Never called on web - [isInAppBrowserSupported] is false. */
@Composable
actual fun InAppBrowser(url: String, modifier: Modifier) = Unit
