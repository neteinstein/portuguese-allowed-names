package org.neteinstein.pickaname.presentation.namelist

import androidx.compose.runtime.Composable

/**
 * No-op on web for now: the in-app browser sheet this guards is Android-only (see
 * [isInAppBrowserSupported]), so there is nothing here for a back gesture to dismiss. Wiring the
 * browser's history/back button into navigation is Phase 3's navigation step.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit
