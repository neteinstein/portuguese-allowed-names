package org.neteinstein.pickaname.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightInversePrimary,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = LightScrim,
    surfaceBright = LightSurfaceBright,
    surfaceDim = LightSurfaceDim,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    primaryFixed = LightPrimaryFixed,
    primaryFixedDim = LightPrimaryFixedDim,
    onPrimaryFixed = LightOnPrimaryFixed,
    onPrimaryFixedVariant = LightOnPrimaryFixedVariant,
    secondaryFixed = LightSecondaryFixed,
    secondaryFixedDim = LightSecondaryFixedDim,
    onSecondaryFixed = LightOnSecondaryFixed,
    onSecondaryFixedVariant = LightOnSecondaryFixedVariant,
    tertiaryFixed = LightTertiaryFixed,
    tertiaryFixedDim = LightTertiaryFixedDim,
    onTertiaryFixed = LightOnTertiaryFixed,
    onTertiaryFixedVariant = LightOnTertiaryFixedVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
    surfaceBright = DarkSurfaceBright,
    surfaceDim = DarkSurfaceDim,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    primaryFixed = DarkPrimaryFixed,
    primaryFixedDim = DarkPrimaryFixedDim,
    onPrimaryFixed = DarkOnPrimaryFixed,
    onPrimaryFixedVariant = DarkOnPrimaryFixedVariant,
    secondaryFixed = DarkSecondaryFixed,
    secondaryFixedDim = DarkSecondaryFixedDim,
    onSecondaryFixed = DarkOnSecondaryFixed,
    onSecondaryFixedVariant = DarkOnSecondaryFixedVariant,
    tertiaryFixed = DarkTertiaryFixed,
    tertiaryFixedDim = DarkTertiaryFixedDim,
    onTertiaryFixed = DarkOnTertiaryFixed,
    onTertiaryFixedVariant = DarkOnTertiaryFixedVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

/**
 * Gender-coding accent roles that live outside Material3's standard [androidx.compose.material3.ColorScheme]
 * (which has no "male"/"female" concept). Kept as an explicit extension of the theme — following the same
 * container/on-container contrast pattern as the standard roles — so this reads as a first-class part of the
 * design system rather than a one-off hardcoded color inside [org.neteinstein.pickaname.presentation.common.GenderTag].
 */
data class PickANameExtendedColors(
    val male: Color,
    val onMale: Color,
    val maleContainer: Color,
    val onMaleContainer: Color,
    val female: Color,
    val onFemale: Color,
    val femaleContainer: Color,
    val onFemaleContainer: Color
)

private val LightExtendedColors = PickANameExtendedColors(
    male = LightMale,
    onMale = LightOnMale,
    maleContainer = LightMaleContainer,
    onMaleContainer = LightOnMaleContainer,
    female = LightFemale,
    onFemale = LightOnFemale,
    femaleContainer = LightFemaleContainer,
    onFemaleContainer = LightOnFemaleContainer
)

private val DarkExtendedColors = PickANameExtendedColors(
    male = DarkMale,
    onMale = DarkOnMale,
    maleContainer = DarkMaleContainer,
    onMaleContainer = DarkOnMaleContainer,
    female = DarkFemale,
    onFemale = DarkOnFemale,
    femaleContainer = DarkFemaleContainer,
    onFemaleContainer = DarkOnFemaleContainer
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

/** Access point for the gender-coding colors, e.g. `PickANameTheme.extendedColors.male`. */
object PickANameTheme {
    val extendedColors: PickANameExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}

/**
 * App-wide Material3 theme, derived from the color palette of irn.justica.gov.pt — the
 * government site that publishes the official Portuguese first-names list this app surfaces.
 *
 * Every [androidx.compose.material3.ColorScheme] role is explicitly supplied (see Color.kt) so
 * no role silently falls back to Material's default baseline palette; the whole app — cards,
 * containers, nav surfaces, scrims — stays tinted to the IRN brand instead of mixing in stock
 * Material purple.
 */
@Composable
fun PickANameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PickANameTypography,
            shapes = PickANameShapes,
            content = content
        )
    }
}
