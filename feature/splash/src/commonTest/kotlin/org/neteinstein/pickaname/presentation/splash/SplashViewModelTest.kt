package org.neteinstein.pickaname.presentation.splash

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.neteinstein.pickaname.domain.repository.NameRepository
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.usecase.ObserveNeedsInitialSyncUseCase
import org.neteinstein.pickaname.util.MainDispatcherHarness
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * In `commonTest`, so it runs on Android, wasmJs and iOS - this is shared code, and a ViewModel
 * that works on one target and not another is exactly the kind of break worth catching. That
 * means hand-written fakes rather than MockK, which is JVM-only.
 */
class SplashViewModelTest {

    private val mainDispatcher = MainDispatcherHarness()

    @BeforeTest
    fun installMainDispatcher() = mainDispatcher.install()

    @AfterTest
    fun uninstallMainDispatcher() = mainDispatcher.uninstall()

    @Test
    fun navigates_to_sync_when_the_database_still_needs_an_initial_population() = runTest(mainDispatcher.dispatcher) {
        val viewModel = SplashViewModel(observeNeedsInitialSync(isEmpty = true))

        advanceUntilIdle()

        assertEquals(SplashDestination.SYNC, viewModel.destination.value)
    }

    @Test
    fun navigates_to_the_name_list_when_the_database_is_already_populated() = runTest(mainDispatcher.dispatcher) {
        val viewModel = SplashViewModel(observeNeedsInitialSync(isEmpty = false))

        advanceUntilIdle()

        assertEquals(SplashDestination.NAME_LIST, viewModel.destination.value)
    }

    @Test
    fun keeps_showing_the_splash_for_the_minimum_duration_even_when_the_check_resolves_instantly() =
        runTest(mainDispatcher.dispatcher) {
            val viewModel = SplashViewModel(observeNeedsInitialSync(isEmpty = false))

            runCurrent()
            assertNull(viewModel.destination.value)

            advanceUntilIdle()
            assertEquals(SplashDestination.NAME_LIST, viewModel.destination.value)
        }

    private fun observeNeedsInitialSync(isEmpty: Boolean) =
        ObserveNeedsInitialSyncUseCase(FakeNameRepository(isEmpty))

    private class FakeNameRepository(private val isEmpty: Boolean) : NameRepository {
        override fun observeNames(filter: NameFilter): Flow<List<NameEntry>> = flowOf(emptyList())
        override fun observeNameCount(filter: NameFilter): Flow<Int> = flowOf(0)
        override fun observeIsEmpty(): Flow<Boolean> = flowOf(isEmpty)
    }
}
