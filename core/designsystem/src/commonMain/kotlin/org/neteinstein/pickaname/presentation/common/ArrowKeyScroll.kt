package org.neteinstein.pickaname.presentation.common

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Scrolls [state] with the up and down arrow keys.
 *
 * This matters on web, and only there: Compose draws to a canvas, so the browser's own arrow-key
 * scrolling never applies to what the user sees, and a page that answers only to a mouse feels
 * broken to anyone using a keyboard. Android and iOS already scroll a focused list with a
 * hardware keyboard, so their actuals do nothing.
 *
 * The topmost surface wins: opening a sheet over the list hands the keys to the sheet, and
 * closing it hands them back - which is what "scroll what I'm looking at" means.
 */
expect fun Modifier.arrowKeyScroll(
    state: ScrollableState,
    step: Dp = DefaultArrowScrollStep
): Modifier

/** About three list rows, which is what one arrow press moves in most desktop lists. */
val DefaultArrowScrollStep: Dp = 72.dp
