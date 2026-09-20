package org.neteinstein.pickaname.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Default Material3 type scale with a slightly bolder, wider-tracked headline/title set to echo
 * the confident, institutional look of the IRN site, without pulling in custom downloadable
 * fonts.
 */
val PickANameTypography = Typography().let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.2.sp
        ),
        titleLarge = base.titleLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.15.sp
        ),
        titleMedium = base.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        labelLarge = base.labelLarge.copy(
            fontWeight = FontWeight.SemiBold
        )
    )
}

/** Extra display style reserved for the splash screen's app name. */
val SplashTitleStyle = TextStyle(
    fontSize = 34.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.4.sp
)
