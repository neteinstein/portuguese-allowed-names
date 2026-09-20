package org.neteinstein.pickaname.app

import androidx.compose.runtime.Composable

/**
 * A prompt to install the native Android app, shown above everything else.
 *
 * Only the web build has anything to show here - and only when the page is being viewed on
 * Android (phone *or* tablet), where there is a native app to install. The Android actual draws
 * nothing at all: suggesting the app you are already inside would be absurd.
 */
@Composable
expect fun InstallAppBanner()
