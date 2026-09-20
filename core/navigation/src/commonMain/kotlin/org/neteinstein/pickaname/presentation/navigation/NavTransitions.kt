package org.neteinstein.pickaname.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

/**
 * Shared Material-motion transitions for [PickANameNavHost].
 *
 * Two families are used across the graph:
 * - "Push" (slide + fade): normal forward/back navigation where a real back stack exists
 *   (NameList <-> Settings). Mirrors standard Android "push a new screen" motion.
 * - "Crossfade": destinations that *replace* everything below them (Splash -> Sync,
 *   Splash/Sync -> NameList via `popUpTo(inclusive = true)`). A slide would visually imply a
 *   back affordance that doesn't exist for these, so a plain fade reads as more honest motion.
 */
private const val MOTION_DURATION_MS = 320
private const val CROSSFADE_EXIT_DURATION_MS = 220

val pushEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 3 },
        animationSpec = tween(MOTION_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(MOTION_DURATION_MS))
}

val pushExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 4 },
        animationSpec = tween(MOTION_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(MOTION_DURATION_MS))
}

val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 4 },
        animationSpec = tween(MOTION_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(MOTION_DURATION_MS))
}

val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / 3 },
        animationSpec = tween(MOTION_DURATION_MS, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(MOTION_DURATION_MS))
}

val crossfadeEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(animationSpec = tween(MOTION_DURATION_MS))
}

val crossfadeExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(animationSpec = tween(CROSSFADE_EXIT_DURATION_MS))
}
