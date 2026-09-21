package org.neteinstein.pickaname

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.neteinstein.pickaname.core.designsystem.resources.Res
import org.neteinstein.pickaname.core.designsystem.resources.app_name
import org.neteinstein.pickaname.core.designsystem.resources.name_list_search_hint
import org.neteinstein.pickaname.core.designsystem.resources.sync_error_title
import org.neteinstein.pickaname.core.designsystem.resources.sync_loading_title

/**
 * End-to-end smoke test: the app must launch and render real content, with the whole Koin graph,
 * a real on-device database and the real (multiplatform) resource system behind it.
 *
 * It asserts that the app reaches **any** of its legitimate first screens, which is what makes it
 * deterministic. The app's first screen depends on state this test can't control:
 * - an empty database (fresh install, and every CI emulator) routes splash → Sync;
 * - a populated one routes splash → the name list;
 * - and the splash itself only shows for [SplashViewModel]'s ~900 ms minimum.
 *
 * That is why this test used to be `@Ignore`d as "flaky": it waited for the app name, which is on
 * the splash and the name list but *not* the sync screen, so on a fresh emulator it was really
 * racing a 900 ms window - and losing. Checking for any of the four texts below removes the race
 * while still failing for the regression that matters: an app that launches to nothing.
 */
@RunWith(AndroidJUnit4::class)
class SplashSmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndShowsItsFirstScreen() {
        val expectedTexts = runBlocking {
            listOf(
                getString(Res.string.app_name),           // splash, and the name list's top bar
                getString(Res.string.sync_loading_title), // first run, while the list downloads
                getString(Res.string.sync_error_title),   // first run with no usable network
                getString(Res.string.name_list_search_hint)
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 30_000) {
            expectedTexts.any { text ->
                composeTestRule.onAllNodesWithText(text, substring = true)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
        }
    }
}
