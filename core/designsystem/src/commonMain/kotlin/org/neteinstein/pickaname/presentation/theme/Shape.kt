package org.neteinstein.pickaname.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * The IRN site favors generously rounded cards (~15dp) and pill-shaped call-to-action buttons.
 * We mirror that here instead of using Material's default (more angular) shape scale.
 */
val PickANameShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(15.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
