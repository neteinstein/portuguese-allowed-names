package org.neteinstein.pickaname.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Swaps [kotlinx.coroutines.Dispatchers.Main] for a [TestDispatcher] for the duration of a test,
 * so code that launches on `viewModelScope` (which dispatches on `Dispatchers.Main.immediate`)
 * can run under `runTest`. Pass [dispatcher] itself as the `context` argument of `runTest(...)`
 * so the test body and anything launched on `Dispatchers.Main` share the same virtual clock -
 * required for `delay`/`debounce`-based logic to advance correctly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}
