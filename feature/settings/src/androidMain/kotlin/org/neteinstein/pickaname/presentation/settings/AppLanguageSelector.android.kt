package org.neteinstein.pickaname.presentation.settings

import androidx.compose.runtime.Composable

/** Null: Android sends the user to the OS per-app language screen instead. */
@Composable
actual fun rememberAppLanguageSelector(): AppLanguageSelector? = null
