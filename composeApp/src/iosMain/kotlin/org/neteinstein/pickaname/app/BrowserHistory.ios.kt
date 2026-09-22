package org.neteinstein.pickaname.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/** Nothing to sync: there is no address bar here, and Back is the nav graph's own business. */
@Composable
actual fun SyncNavigationWithPlatformHistory(navController: NavHostController) = Unit

actual fun platformStartRoute(): String? = null
