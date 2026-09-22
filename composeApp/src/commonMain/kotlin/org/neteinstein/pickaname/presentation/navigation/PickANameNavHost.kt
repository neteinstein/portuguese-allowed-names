package org.neteinstein.pickaname.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.neteinstein.pickaname.app.SyncNavigationWithPlatformHistory
import org.neteinstein.pickaname.app.platformStartRoute
import org.neteinstein.pickaname.presentation.namelist.NameListScreen
import org.neteinstein.pickaname.presentation.settings.SettingsScreen
import org.neteinstein.pickaname.presentation.splash.SplashScreen
import org.neteinstein.pickaname.presentation.sync.SyncScreen

/**
 * App-wide nav graph: Splash decides between Sync (empty DB) and the name list; Settings can
 * trigger a fresh Sync when the source URL changes; a successful Sync always lands on the name
 * list with the whole back stack cleared, so the user never lands back on Splash/Sync via "back".
 */
@Composable
fun PickANameNavHost(
    logo: Painter,
    navController: NavHostController = rememberNavController()
) {
    // Keeps the browser's address bar, Back button and reload in step with the graph; a no-op
    // on Android and iOS (see SyncNavigationWithPlatformHistory).
    SyncNavigationWithPlatformHistory(navController)

    NavHost(
        navController = navController,
        // A shared link or a reload can name the screen to open on; every platform but web
        // always answers null and gets the normal first-launch flow.
        startDestination = platformStartRoute() ?: Routes.SPLASH,
        enterTransition = pushEnter,
        exitTransition = pushExit,
        popEnterTransition = popEnter,
        popExitTransition = popExit
    ) {
        composable(
            route = Routes.SPLASH,
            exitTransition = crossfadeExit
        ) {
            SplashScreen(
                onNavigateToSync = {
                    navController.navigate(Routes.sync(SyncOrigin.ONBOARDING)) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToNameList = {
                    navController.navigate(Routes.NAME_LIST) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                logo = logo
            )
        }
        composable(
            route = Routes.SYNC_PATTERN,
            arguments = listOf(navArgument("origin") { type = NavType.StringType }),
            enterTransition = {
                // Coming from Splash is a stack replace (crossfade); coming from Settings is a
                // real forward push (slide), since Settings remains underneath on the back stack.
                if (initialState.destination.route == Routes.SPLASH) crossfadeEnter() else pushEnter()
            },
            exitTransition = crossfadeExit
        ) {
            SyncScreen(
                onSyncSuccess = {
                    navController.navigate(Routes.NAME_LIST) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onEditSource = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        composable(
            route = Routes.NAME_LIST,
            enterTransition = crossfadeEnter
        ) {
            NameListScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onSourceUpdated = {
                    navController.navigate(Routes.sync(SyncOrigin.SETTINGS))
                }
            )
        }
    }
}
