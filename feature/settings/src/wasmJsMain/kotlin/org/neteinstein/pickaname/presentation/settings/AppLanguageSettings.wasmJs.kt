package org.neteinstein.pickaname.presentation.settings

import androidx.compose.runtime.Composable

/**
 * Null on web: a browser has no per-app language setting to hand the user off to - the page
 * follows the browser's own language preference - so the Settings screen omits that card here
 * rather than offering a button with nowhere to go.
 */
@Composable
actual fun rememberAppLanguageSettingsLauncher(): (() -> Unit)? = null
