package org.neteinstein.pickaname.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/**
 * Keeps the platform's own idea of "where am I" in step with the nav graph.
 *
 * Only the web has one: a browser has an address bar, a back button and a reload, and an app
 * that ignores them feels broken - every screen looks like the same URL, refresh throws you back
 * to the splash, and Back leaves the site entirely. Android and iOS have nothing to sync here;
 * their back handling is the nav graph's own.
 */
@Composable
expect fun SyncNavigationWithPlatformHistory(navController: NavHostController)

/**
 * The route to start on, when the platform says so - a deep link. `null` means "use the graph's
 * own start destination", which is what every platform but web always answers.
 */
expect fun platformStartRoute(): String?
