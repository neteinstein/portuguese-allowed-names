package org.neteinstein.pickaname.presentation.common

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Nothing to add: a focused list already scrolls with a hardware keyboard's arrow keys here, and
 * the overwhelming case on this platform is touch.
 */
actual fun Modifier.arrowKeyScroll(state: ScrollableState, step: Dp): Modifier = this
