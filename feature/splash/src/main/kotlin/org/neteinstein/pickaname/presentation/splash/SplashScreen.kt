package org.neteinstein.pickaname.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.annotation.DrawableRes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.neteinstein.pickaname.presentation.theme.PickANameGradients
import org.neteinstein.pickaname.presentation.theme.SplashTitleStyle
import org.jetbrains.compose.resources.stringResource
import org.neteinstein.pickaname.core.designsystem.resources.Res
import org.neteinstein.pickaname.core.designsystem.resources.app_name
import org.neteinstein.pickaname.core.designsystem.resources.splash_tagline
import org.neteinstein.pickaname.core.designsystem.resources.splash_unofficial_disclaimer

/**
 * First screen shown on app launch. Purely a branded loading moment: it decides (via
 * [SplashViewModel]) whether the DB still needs its initial population and then navigates
 * accordingly, without any user interaction.
 *
 * This is the *second* stage of the splash experience: the app's `MainActivity` already shows
 * the system `core-splashscreen` (brand color + static icon) for the instant between process
 * start and the first Compose frame, then this composable takes over with the full animated
 * brand moment for [SplashViewModel]'s minimum duration.
 *
 * [logoRes] is supplied by the caller rather than looked up here: this module has no dependency
 * on `:app` (feature modules never depend on the app shell - see MIGRATION_PLAN.md §3.1), and the
 * launcher icon is app-identity, not shared design-system UI, so it can't live in
 * `core:designsystem` either.
 */
@Composable
fun SplashScreen(
    onNavigateToSync: () -> Unit,
    onNavigateToNameList: () -> Unit,
    @DrawableRes logoRes: Int,
    viewModel: SplashViewModel = koinViewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.SYNC -> onNavigateToSync()
            SplashDestination.NAME_LIST -> onNavigateToNameList()
            null -> Unit
        }
    }

    SplashContent(logoRes = logoRes)
}

@Composable
private fun SplashContent(@DrawableRes logoRes: Int) {
    var showLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showLogo = true
        delay(250)
        showText = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Text/progress colors are deliberately literal white, not theme-driven: the brand gradient
    // background stays the same punchy IRN blue in both light and dark mode (see
    // PickANameGradients.splashBackground), so the foreground needs a fixed, guaranteed-contrast
    // color rather than MaterialTheme.colorScheme.onPrimary, which is tuned for the *app's*
    // light/dark surfaces, not this specific gradient.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PickANameGradients.splashBackground()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(tween(450)) + scaleIn(
                    initialScale = 0.6f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .scale(pulseScale)
                            .alpha(0.25f)
                            .background(Color.White, CircleShape)
                    )
                    Image(
                        painter = painterResource(logoRes),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedVisibility(
                visible = showText,
                enter = fadeIn(tween(450)) + slideInVertically(initialOffsetY = { it / 3 })
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = SplashTitleStyle,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(Res.string.splash_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.splash_unofficial_disclaimer),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 3.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .size(28.dp)
        )
    }
}
