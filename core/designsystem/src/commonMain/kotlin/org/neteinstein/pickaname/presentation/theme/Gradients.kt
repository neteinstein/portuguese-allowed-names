package org.neteinstein.pickaname.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

/**
 * Reusable brand gradients for hero-style surfaces (splash screen, sync loading state).
 * Centralized here so any new "big blue moment" in the app stays visually consistent.
 */
object PickANameGradients {

    /** Deep-to-brand-blue diagonal sweep, used behind the splash brand mark. */
    @Composable
    fun splashBackground(): Brush {
        val colors = if (isSystemInDarkTheme()) {
            listOf(DarkBackground, IrnBlueNavy, DarkPrimaryContainer)
        } else {
            listOf(IrnBlueNavy, IrnBlue, IrnBlueSecondary)
        }
        return Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset.Infinite
        )
    }

    /** Subtle brand-tinted sweep for elevated hero cards (e.g. sync screen). */
    @Composable
    fun heroCard(): Brush {
        val colors = if (isSystemInDarkTheme()) {
            listOf(DarkPrimaryContainer, DarkSecondaryContainer)
        } else {
            listOf(LightPrimaryContainer, LightSecondaryContainer)
        }
        return Brush.linearGradient(colors = colors)
    }
}
