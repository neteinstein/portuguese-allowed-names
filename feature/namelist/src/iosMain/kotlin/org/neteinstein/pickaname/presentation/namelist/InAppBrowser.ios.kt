package org.neteinstein.pickaname.presentation.namelist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberExternalUrlOpener(): (String) -> Unit = remember {
    { url ->
        NSURL.URLWithString(url)?.let { UIApplication.sharedApplication.openURL(it) }
    }
}

/**
 * False for now. iOS *does* have an in-app browser (`SFSafariViewController`), and it would be
 * the natural equivalent of Android's WebView sheet - but hosting a UIKit view controller inside
 * this Compose sheet is real work that deserves a real device to verify on, so the meaning search
 * opens in Safari until then.
 */
@Composable
actual fun isInAppBrowserSupported(): Boolean = false

/** Never called on iOS - [isInAppBrowserSupported] is false. */
@Composable
actual fun InAppBrowser(url: String, modifier: Modifier) = Unit
