@file:OptIn(ExperimentalTestApi::class)

package org.neteinstein.pickaname.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.runComposeUiTest
import org.koin.core.context.stopKoin
import org.koin.core.context.startKoin
import org.neteinstein.pickaname.di.appModules
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Starts the real app - real Koin graph, real navigation, real resources - in a real browser and
 * asserts it renders something.
 *
 * This is the check that was missing when a Compose version skew shipped an app that compiled
 * perfectly and then died on load with `IrLinkageError: Function 'ComposeViewport' can not be
 * called` (MIGRATION_PLAN.md §9.1). No compile-time check can catch that class of bug; only
 * composing the thing can. It deliberately asserts almost nothing about *what* is on screen -
 * the ViewModel suites cover behaviour - because its job is to fail when the app cannot start at
 * all.
 */
private val ANY_NODE = SemanticsMatcher("any node") { true }

class AppRuntimeSmokeTest {

    @AfterTest
    fun stopKoinAfterTest() {
        // The Koin graph is process-global; leaving it running would make a second test in this
        // binary fail with "A Koin Application has already been started".
        runCatching { stopKoin() }
    }

    @Test
    fun the_app_starts_and_renders() = runComposeUiTest {
        startKoin { modules(appModules()) }

        setContent {
            App(logo = BitmapPainter(ImageBitmap(1, 1)))
        }

        // Not waitForIdle(): the start destination shows an indeterminate progress indicator
        // while it works, and an animation that never ends means the composition is never idle -
        // so waiting for idle waits forever. Waiting for the nodes themselves asks the question
        // this test actually cares about.
        waitUntil(timeoutMillis = 10_000) {
            onAllNodes(ANY_NODE).fetchSemanticsNodes().isNotEmpty()
        }

        // Any node at all: reaching this point means the whole graph composed - theme, nav host,
        // the start destination and its ViewModel - without throwing.
        val rendered = onAllNodes(ANY_NODE).fetchSemanticsNodes()
        assertTrue(rendered.isNotEmpty(), "App() composed no nodes at all")
    }
}
