package org.neteinstein.pickaname.presentation.common

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

/**
 * Listens for the arrow keys on the window rather than through Compose's focus system.
 *
 * Focus was the obvious route and it does work for the list, but not for content inside a
 * `ModalBottomSheet`: the sheet's popup never hands focus to the content, so the keys went
 * nowhere and the sheet stayed put. Listening where the browser actually delivers the event
 * sidesteps that, and means nothing has to be clicked first.
 *
 * Registered surfaces form a stack, so the most recently composed one - the sheet over the list -
 * receives the keys, and disposing it hands them back to the list underneath.
 */
actual fun Modifier.arrowKeyScroll(state: ScrollableState, step: Dp): Modifier = composed {
    val scope = rememberCoroutineScope()
    val stepPx = with(LocalDensity.current) { step.toPx() }

    DisposableEffect(state, stepPx) {
        val target = ArrowScrollTarget(state, scope, stepPx)
        ArrowKeyScrollRegistry.register(target)
        onDispose { ArrowKeyScrollRegistry.unregister(target) }
    }

    this
}

private class ArrowScrollTarget(
    val state: ScrollableState,
    val scope: CoroutineScope,
    val stepPx: Float
)

/**
 * Whether the browser's focus is inside a text entry - Compose's own hidden input while the user
 * is typing in a text field, or anything else on the page that takes typed characters.
 */
private fun isTextEntryFocused(): Boolean {
    val active = document.activeElement ?: return false
    if ((active as? HTMLElement)?.isContentEditable == true) return true
    return active.tagName.lowercase() in TEXT_ENTRY_TAGS
}

private val TEXT_ENTRY_TAGS = setOf("input", "textarea")

private object ArrowKeyScrollRegistry {

    private val targets = mutableListOf<ArrowScrollTarget>()
    private var listening = false

    fun register(target: ArrowScrollTarget) {
        targets += target
        if (!listening) {
            window.addEventListener("keydown", ::onKeyDown)
            listening = true
        }
    }

    fun unregister(target: ArrowScrollTarget) {
        targets -= target
    }

    private fun onKeyDown(event: Event) {
        val keyboardEvent = event as? KeyboardEvent ?: return
        val direction = when (keyboardEvent.key) {
            "ArrowDown" -> 1f
            "ArrowUp" -> -1f
            else -> return
        }
        // A modifier held down means the user is asking the browser for something else.
        if (keyboardEvent.altKey || keyboardEvent.ctrlKey || keyboardEvent.metaKey) return
        // Compose routes text input through a real hidden <input>/<textarea>; while one has focus
        // the arrows belong to the caret, not to us.
        if (isTextEntryFocused()) return

        val target = targets.lastOrNull() ?: return
        event.preventDefault()
        target.scope.launch { target.state.animateScrollBy(direction * target.stepPx) }
    }
}
