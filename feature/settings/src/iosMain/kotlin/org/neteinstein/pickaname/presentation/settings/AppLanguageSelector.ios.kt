package org.neteinstein.pickaname.presentation.settings

import androidx.compose.runtime.Composable

/** Null: iOS sends the user to the app's own page in Settings instead. */
@Composable
actual fun rememberAppLanguageSelector(): AppLanguageSelector? = null
