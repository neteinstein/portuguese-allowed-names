package org.neteinstein.pickaname.presentation.namelist

import androidx.compose.runtime.Composable

/**
 * No-op: iOS has no system back button, and the sheet this guards is dismissed by its own
 * swipe/tap-outside gestures. (It is only ever shown where [isInAppBrowserSupported] is true,
 * which iOS isn't.)
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
