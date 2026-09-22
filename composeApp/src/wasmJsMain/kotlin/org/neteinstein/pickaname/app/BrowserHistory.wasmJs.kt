@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.neteinstein.pickaname.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import kotlinx.browser.window
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import org.neteinstein.pickaname.presentation.navigation.Routes
import org.w3c.dom.events.Event

/**
 * Two-way sync between the nav graph and the browser's history.
 *
 * Forward: every destination change replaces/pushes a hash URL (`#/name_list`), so the address
 * bar names the screen you are on, a reload returns to it, and a link can be shared.
 *
 * Backward: `popstate` (the Back and Forward buttons) navigates the graph instead of leaving the
 * site.
 *
 * Hash URLs rather than real paths because this ships to GitHub Pages, which serves static files
 * only: `/settings` would be a 404 on reload, `#/settings` is always `index.html`.
 */
@Composable
actual fun SyncNavigationWithPlatformHistory(navController: NavHostController) {
    // Graph -> address bar.
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow
            .map { it.destination.route }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { route ->
                val target = "#/$route"
                if (window.location.hash != target) {
                    // pushState, not assignment: assigning to location.hash would fire our own
                    // popstate listener back at us and fight the navigation that just happened.
                    window.history.pushState(null, "", target)
                }
            }
    }

    // Address bar -> graph.
    DisposableEffect(navController) {
        val listener: (Event) -> Unit = {
            routeFromHash()?.let { route ->
                if (navController.currentDestination?.route != route) {
                    navController.navigate(route) {
                        // Back should return to where the user was, not stack another copy of it.
                        launchSingleTop = true
                    }
                }
            }
        }
        window.addEventListener("popstate", listener)
        onDispose { window.removeEventListener("popstate", listener) }
    }
}

/**
 * A reload or a shared link lands on whatever the hash names, as long as it is a screen worth
 * starting on. Splash and sync are deliberately excluded: they are transitions, not destinations,
 * and starting on them would mean re-running the first-launch decision the graph makes anyway.
 */
actual fun platformStartRoute(): String? =
    routeFromHash()?.takeIf { it in DEEP_LINKABLE_ROUTES }

private fun routeFromHash(): String? =
    window.location.hash.removePrefix("#/").takeIf { it.isNotBlank() }

private val DEEP_LINKABLE_ROUTES = setOf(Routes.NAME_LIST, Routes.SETTINGS)
