package org.neteinstein.pickaname.presentation.namelist

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.view.doOnLayout

@Composable
actual fun rememberExternalUrlOpener(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) { { url -> openUrlInCustomTab(context, url) } }
}

@Composable
actual fun isInAppBrowserSupported(): Boolean {
    val context = LocalContext.current
    return remember(context) { canLoadWebView(context) }
}

@Composable
actual fun InAppBrowser(url: String, modifier: Modifier) {
    var isLoading by remember(url) { mutableStateOf(true) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(android.graphics.Color.WHITE)
                    settings.javaScriptEnabled = true
                    // Modern search result pages (e.g. DuckDuckGo's AI chat answer) use
                    // localStorage/sessionStorage during their own startup; without this they throw
                    // and can fail to render at all.
                    settings.domStorageEnabled = true
                    // Chromium composites the WebView's frames on its own render thread and
                    // delivers them asynchronously; invalidating on every loading progress tick
                    // keeps pulling each newly composited frame onto the screen as it arrives,
                    // for the initial load and any in-sheet navigation to another result.
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView, newProgress: Int) {
                            view.invalidate()
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageCommitVisible(view: WebView, url: String) {
                            view.invalidate()
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView,
                            request: WebResourceRequest,
                            error: WebResourceError
                        ) {
                            if (request.isForMainFrame) isLoading = false
                        }
                    }
                    // Without this, the bottom sheet's own drag handling steals vertical swipes
                    // that start over the WebView, making it impossible to scroll the page inside
                    // it (e.g. to reach a cookie-consent button below the fold).
                    setOnTouchListener { view, _ ->
                        view.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    // Some pages read the viewport size while they first run (e.g. to size a
                    // fixed-position layout) and never recompute it later; loading before this
                    // WebView has been measured hands them a 0x0 viewport and leaves their layout
                    // collapsed even after it's resized to its real bounds.
                    doOnLayout {
                        loadUrl(url)
                    }
                }
            }
        )
        // Until the page has something to show, the WebView is just a blank white panel, which
        // on a slow connection looks like the sheet is broken.
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

/**
 * Opens [url] in a Chrome Custom Tab so it's more likely to reuse the user's logged-in browser
 * session (helps avoid Google/DuckDuckGo cookie-consent overlays a "cold" browser context would
 * otherwise show). Falls back to a plain VIEW intent if no Custom Tabs provider is available.
 */
private fun openUrlInCustomTab(context: Context, url: String) {
    try {
        CustomTabsIntent.Builder().build().launchUrl(context, url.toUri())
    } catch (e: ActivityNotFoundException) {
        openUrl(context, url)
    }
}

/** Opens [url] in an external browser; silently no-ops if no app can handle it. */
private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: ActivityNotFoundException) {
        // No browser available on this device - nothing else we can do here.
    }
}

/**
 * Whether this device can actually host a [WebView]. Some OEM/enterprise-restricted devices ship
 * without a working WebView provider, which throws when instantiated - in that case the in-app
 * bottom sheet isn't an option and callers fall back to opening the search externally.
 */
private fun canLoadWebView(context: Context): Boolean =
    try {
        WebView(context)
        true
    } catch (e: Throwable) {
        false
    }
