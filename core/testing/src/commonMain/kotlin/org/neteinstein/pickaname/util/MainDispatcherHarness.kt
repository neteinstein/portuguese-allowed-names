package org.neteinstein.pickaname.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/**
 * Swaps `Dispatchers.Main` for a [TestDispatcher] for the duration of a test, so code that
 * launches on `viewModelScope` (which dispatches on `Dispatchers.Main.immediate`) can run under
 * `runTest`. Pass [dispatcher] as `runTest`'s context so the test body and anything launched on
 * `Dispatchers.Main` share one virtual clock - required for `delay`/`debounce` logic to advance.
 *
 * The multiplatform counterpart of [MainDispatcherRule] (JUnit rules are JVM-only, and these
 * tests run on wasmJs and iOS too). Install and uninstall from `@BeforeTest`/`@AfterTest` rather
 * than around the `runTest` body: a `stateIn(viewModelScope, ...)` job can still be dispatching
 * while `runTest` drains its scheduler, and resetting `Main` before that finishes makes those
 * dispatches throw.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherHarness {

    val dispatcher: TestDispatcher = StandardTestDispatcher()

    fun install() {
        Dispatchers.setMain(dispatcher)
    }

    fun uninstall() {
        Dispatchers.resetMain()
    }
}
