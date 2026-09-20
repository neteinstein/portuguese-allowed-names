package org.neteinstein.pickaname.presentation.namelist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Opens a URL outside the app: a Chrome Custom Tab (falling back to a plain browser intent) on
 * Android, a new browser tab on web.
 */
@Composable
expect fun rememberExternalUrlOpener(): (String) -> Unit

/**
 * Whether this platform can show a web page *inside* the app, in the name-meaning bottom sheet.
 *
 * Android: yes, unless the device ships without a usable WebView provider (some OEM/enterprise
 * builds do, and instantiating one throws). Web: no - search engines send `X-Frame-Options`/
 * `frame-ancestors` headers that stop their result pages being embedded in another page, so the
 * web build always opens the search in a new tab instead of a sheet that could only ever be
 * blank.
 */
@Composable
expect fun isInAppBrowserSupported(): Boolean

/**
 * Renders [url] inside the app. Only called where [isInAppBrowserSupported] is true.
 */
@Composable
expect fun InAppBrowser(url: String, modifier: Modifier)
