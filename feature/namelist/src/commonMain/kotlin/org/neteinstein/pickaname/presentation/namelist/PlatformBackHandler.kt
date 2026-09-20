package org.neteinstein.pickaname.presentation.namelist

import androidx.compose.runtime.Composable

/**
 * Intercepts the platform's "back" gesture while [enabled], used to dismiss the name-meaning
 * sheet with the system back button instead of closing the screen behind it.
 *
 * Expect/actual rather than a shared implementation: Compose Multiplatform 1.8.2 has no common
 * `BackHandler` (it arrives in a later release), and the browser's equivalent - intercepting the
 * history back button - belongs with the Phase 3 navigation work rather than being faked here.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
