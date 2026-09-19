package org.neteinstein.pickaname.presentation.theme

import androidx.compose.ui.graphics.Color

// Colors sourced from irn.justica.gov.pt (Instituto dos Registos e do Notariado), the
// government site that publishes the official Portuguese first-names list. Verified directly
// against the site's stylesheet (.theme--IRN rules in dnnthemeportaljustica/css/main.css):
// Brand blue family (.theme--IRN):
val IrnBlue = Color(0xFF00599D)
val IrnBlueDark = Color(0xFF002E51)
val IrnBlueHover = Color(0xFF003C6A)
val IrnBlueSecondary = Color(0xFF086B9C)
// Shared justica.gov.pt network accent:
val IrnGold = Color(0xFFF0B21D)

// Every tone below was generated from those exact brand hues through a perceptual (CIE Lab)
// tonal ramp, following the same tone->role recipe Material3's own baseline scheme uses
// (see androidx.compose.material3.tokens.ColorLightTokens/ColorDarkTokens). This guarantees
// *every* ColorScheme role is brand-tinted — including surfaceContainer tiers, scrim, the
// inverse/fixed roles, etc. — instead of silently falling back to Material's default purple
// when a role is left unspecified. `primary`/`secondary`/`tertiary` are pinned to the literal
// IRN hex above; everything else is a consistent derivative of it.

// --- Light color scheme ---
val LightPrimary = IrnBlue
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD0E3FF)
val LightOnPrimaryContainer = Color(0xFF001C39)
val LightInversePrimary = Color(0xFF98C8FF)

val LightSecondary = IrnBlueSecondary
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFC9E6FF)
val LightOnSecondaryContainer = Color(0xFF001E32)

// Tertiary intentionally keeps the literal bright gold as the *background* tone (matching the
// shared justica.gov.pt CTA color exactly) with a dark-brown foreground for contrast — the
// algorithmic tone40 would otherwise darken the gold into an unrecognizable brown.
val LightTertiary = IrnGold
val LightOnTertiary = Color(0xFF5C2300)
val LightTertiaryContainer = Color(0xFFFFDA75)
val LightOnTertiaryContainer = Color(0xFF381400)

val LightBackground = Color(0xFFF9F9FA)
val LightOnBackground = Color(0xFF1A1B1E)
val LightSurface = Color(0xFFF9F9FA)
val LightOnSurface = Color(0xFF1A1B1E)
val LightSurfaceVariant = Color(0xFFDFE2EB)
val LightOnSurfaceVariant = Color(0xFF404755)
val LightOutline = Color(0xFF707787)
val LightOutlineVariant = Color(0xFFBFC6D8)
val LightInverseSurface = Color(0xFF2E3037)
val LightInverseOnSurface = Color(0xFFF0F1F3)
val LightScrim = Color(0xFF000000)

val LightSurfaceBright = Color(0xFFF9F9FA)
val LightSurfaceDim = Color(0xFFD8DADF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF3F3F5)
val LightSurfaceContainer = Color(0xFFEDEEF0)
val LightSurfaceContainerHigh = Color(0xFFE7E8EB)
val LightSurfaceContainerHighest = Color(0xFFE1E2E6)

val LightPrimaryFixed = Color(0xFFD0E3FF)
val LightPrimaryFixedDim = Color(0xFF98C8FF)
val LightOnPrimaryFixed = Color(0xFF001C39)
val LightOnPrimaryFixedVariant = Color(0xFF00498A)
val LightSecondaryFixed = Color(0xFFC9E6FF)
val LightSecondaryFixedDim = Color(0xFF8ACDFF)
val LightOnSecondaryFixed = Color(0xFF001E32)
val LightOnSecondaryFixedVariant = Color(0xFF004D7B)
val LightTertiaryFixed = Color(0xFFFFDA75)
val LightTertiaryFixedDim = Color(0xFFFFB800)
val LightOnTertiaryFixed = Color(0xFF381400)
val LightOnTertiaryFixedVariant = Color(0xFF733A00)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// --- Dark color scheme ---
val DarkPrimary = Color(0xFF98C8FF)
val DarkOnPrimary = Color(0xFF003370)
val DarkPrimaryContainer = Color(0xFF00498A)
val DarkOnPrimaryContainer = Color(0xFFD0E3FF)
val DarkInversePrimary = Color(0xFF1760A5)

val DarkSecondary = Color(0xFF8ACDFF)
val DarkOnSecondary = Color(0xFF003662)
val DarkSecondaryContainer = Color(0xFF004D7B)
val DarkOnSecondaryContainer = Color(0xFFC9E6FF)

val DarkTertiary = Color(0xFFFFB800)
val DarkOnTertiary = Color(0xFF5C2300)
val DarkTertiaryContainer = Color(0xFF733A00)
val DarkOnTertiaryContainer = Color(0xFFFFDA75)

val DarkBackground = Color(0xFF131315)
val DarkOnBackground = Color(0xFFE1E2E6)
val DarkSurface = Color(0xFF131315)
val DarkOnSurface = Color(0xFFE1E2E6)
val DarkSurfaceVariant = Color(0xFF404755)
val DarkOnSurfaceVariant = Color(0xFFBFC6D8)
val DarkOutline = Color(0xFF8A91A1)
val DarkOutlineVariant = Color(0xFF404755)
val DarkInverseSurface = Color(0xFFE1E2E6)
val DarkInverseOnSurface = Color(0xFF2E3037)
val DarkScrim = Color(0xFF000000)

val DarkSurfaceBright = Color(0xFF37393F)
val DarkSurfaceDim = Color(0xFF131315)
val DarkSurfaceContainerLowest = Color(0xFF0D0E10)
val DarkSurfaceContainerLow = Color(0xFF1A1B1E)
val DarkSurfaceContainer = Color(0xFF1E2023)
val DarkSurfaceContainerHigh = Color(0xFF282A2F)
val DarkSurfaceContainerHighest = Color(0xFF32353B)

val DarkPrimaryFixed = Color(0xFFD0E3FF)
val DarkPrimaryFixedDim = Color(0xFF98C8FF)
val DarkOnPrimaryFixed = Color(0xFF001C39)
val DarkOnPrimaryFixedVariant = Color(0xFF00498A)
val DarkSecondaryFixed = Color(0xFFC9E6FF)
val DarkSecondaryFixedDim = Color(0xFF8ACDFF)
val DarkOnSecondaryFixed = Color(0xFF001E32)
val DarkOnSecondaryFixedVariant = Color(0xFF004D7B)
val DarkTertiaryFixed = Color(0xFFFFDA75)
val DarkTertiaryFixedDim = Color(0xFFFFB800)
val DarkOnTertiaryFixed = Color(0xFF381400)
val DarkOnTertiaryFixedVariant = Color(0xFF733A00)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// --- Gender accent colors ---
// Explicit convention (by design request): male = blue, female = pink. Kept as dedicated roles
// rather than reusing secondary/tertiary so gender coding stays a stable, unambiguous signal
// wherever it shows up (name list tags, gender filter chips) independent of brand-color reuse
// elsewhere. Generated with the same Lab tonal-ramp recipe as the brand roles above.
val LightMale = Color(0xFF0952DD)
val LightOnMale = Color(0xFFFFFFFF)
val LightMaleContainer = Color(0xFFDEDAFF)
val LightOnMaleContainer = Color(0xFF001650)

val LightFemale = Color(0xFFC0005F)
val LightOnFemale = Color(0xFFFFFFFF)
val LightFemaleContainer = Color(0xFFFFC9E2)
val LightOnFemaleContainer = Color(0xFF43001C)

val DarkMale = Color(0xFFABB7FF)
val DarkOnMale = Color(0xFF0027A4)
val DarkMaleContainer = Color(0xFF003BC0)
val DarkOnMaleContainer = Color(0xFFDEDAFF)

val DarkFemale = Color(0xFFFF87C7)
val DarkOnFemale = Color(0xFF830031)
val DarkFemaleContainer = Color(0xFFA10048)
val DarkOnFemaleContainer = Color(0xFFFFC9E2)

// --- Extra brand tones reserved for gradients (hero/splash surfaces) ---
val IrnBlueNavy = Color(0xFF001C39) // Primary tone 10 — deep gradient anchor
val IrnBlueDeep = Color(0xFF003370) // Primary tone 20
val IrnBlueSky = Color(0xFF98C8FF) // Primary tone 80 — light gradient highlight
