package org.neteinstein.pickaname.presentation.namelist

import app.cash.turbine.test
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.usecase.ObserveNameCountUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveNamesUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.RefreshNamesIfDueUseCase
import org.neteinstein.pickaname.domain.usecase.SyncNamesUseCase
import org.neteinstein.pickaname.fake.FakeNameRepository
import org.neteinstein.pickaname.fake.FakeNameSyncRepository
import org.neteinstein.pickaname.fake.FakeSettingsRepository
import org.neteinstein.pickaname.util.MainDispatcherHarness
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [NameListViewModel.uiState] is backed by `stateIn(..., NameListUiState())`, so the very first
 * item any collector sees is always that static initial value - before the real data has had a
 * chance to flow through `combine`/`flatMapLatest`. Every test below accounts for that by
 * awaiting it explicitly, then calling `runCurrent()` to let the real pipeline compute its first
 * genuine result, before asserting on anything.
 *
 * In `commonTest`, so it runs on Android, wasmJs and iOS: this is shared code, and the debounce
 * timing below is exactly the sort of thing that could behave differently on another dispatcher.
 * The use cases are real, with fakes underneath, so auto-refresh outcomes are set up by putting
 * the fakes in the right state rather than by stubbing the use case's return value.
 */
class NameListViewModelTest {

    private val mainDispatcher = MainDispatcherHarness()

    @BeforeTest
    fun installMainDispatcher() = mainDispatcher.install()

    @AfterTest
    fun uninstallMainDispatcher() = mainDispatcher.uninstall()

    private val alice = NameEntry(1, "Alice", Gender.FEMALE)
    private val bob = NameEntry(2, "Bob", Gender.MALE)
    private val kevin = NameEntry(3, "Kevin", Gender.MALE)
    private val allNames = listOf(alice, bob)
    private val fixedNow = 1_700_000_000_000L

    @Test
    fun initial_state_exposes_the_unfiltered_list_and_total_count() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // stateIn's static default, emitted before the pipeline has run at all
            runCurrent()

            val state = awaitItem()
            assertEquals(listOf(alice, bob), state.names)
            assertEquals(2, state.count)
            assertNull(state.selectedGender)
            assertNull(state.selectedInitial)
        }
    }

    @Test
    fun selecting_a_gender_filters_immediately_without_waiting_for_debounce() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // static default
            runCurrent()
            awaitItem() // real unfiltered first result

            viewModel.onGenderSelected(Gender.MALE)
            runCurrent()

            val state = awaitItem()
            assertEquals(listOf(bob), state.names)
            assertEquals(1, state.count)
            assertEquals(Gender.MALE, state.selectedGender)
        }
    }

    @Test
    fun selecting_an_initial_letter_filters_immediately() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // static default
            runCurrent()
            awaitItem() // real unfiltered first result

            viewModel.onInitialSelected('B')
            runCurrent()

            val state = awaitItem()
            assertEquals(listOf(bob), state.names)
            assertEquals('B', state.selectedInitial)
        }
    }

    @Test
    fun enabling_traditionalOnly_excludes_names_that_fail_the_heuristic() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(names = listOf(alice, bob, kevin))

        viewModel.uiState.test {
            awaitItem() // static default
            runCurrent()
            awaitItem() // real unfiltered first result

            viewModel.onTraditionalOnlyChanged(true)
            runCurrent()

            val state = awaitItem()
            assertEquals(listOf(alice), state.names)
            assertEquals(1, state.count)
            assertTrue(state.traditionalOnly)
        }
    }

    @Test
    fun a_gender_selection_made_before_the_first_result_arrives_still_applies_immediately() =
        runTest(mainDispatcher.dispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // static default

                // Selecting a filter before the pipeline has produced its first real value at
                // all (e.g. a very fast tap right as the screen opens) must not be lost,
                // delayed, or preceded by a transient unfiltered result.
                viewModel.onGenderSelected(Gender.MALE)
                runCurrent()

                val state = awaitItem()
                assertEquals(listOf(bob), state.names)
                assertEquals(Gender.MALE, state.selectedGender)
            }
        }

    @Test
    fun typing_a_query_echoes_immediately_but_results_wait_for_the_debounce_window() =
        runTest(mainDispatcher.dispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // static default
                runCurrent()
                awaitItem() // real unfiltered first result

                viewModel.onQueryChange("bob")
                runCurrent()

                // The search field must reflect the keystroke right away, even though the list
                // hasn't been refiltered yet - otherwise typing looks like it does nothing.
                val echoed = awaitItem()
                assertEquals("bob", echoed.query)
                assertEquals(listOf(alice, bob), echoed.names)

                advanceTimeBy(300)
                runCurrent()

                val state = awaitItem()
                assertEquals(listOf(bob), state.names)
                assertEquals("bob", state.query)
            }
        }

    @Test
    fun rapid_query_changes_echo_every_keystroke_but_only_refilter_once_the_debounce_settles() =
        runTest(mainDispatcher.dispatcher) {
            val viewModel = createViewModel()

            viewModel.uiState.test {
                awaitItem() // static default
                runCurrent()
                awaitItem() // real unfiltered first result

                viewModel.onQueryChange("a")
                runCurrent()
                assertEquals("a", awaitItem().query)

                advanceTimeBy(100)
                viewModel.onQueryChange("al")
                runCurrent()
                assertEquals("al", awaitItem().query)

                advanceTimeBy(100)
                viewModel.onQueryChange("ali")
                runCurrent()
                val lastEcho = awaitItem()
                assertEquals("ali", lastEcho.query)
                assertEquals(listOf(alice, bob), lastEcho.names) // not refiltered yet

                advanceTimeBy(300)
                runCurrent()

                val state = awaitItem()
                assertEquals("ali", state.query)
                assertEquals(listOf(alice), state.names)
            }
        }

    @Test
    fun does_not_emit_an_event_when_no_auto_refresh_is_due() = runTest(mainDispatcher.dispatcher) {
        // Last refresh "just happened", so nothing is due.
        val viewModel = createViewModel(lastRefreshTimestamp = fixedNow)

        viewModel.events.test {
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun does_not_emit_an_event_when_the_auto_refresh_succeeds() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(
            lastRefreshTimestamp = null, // never refreshed, so a refresh is due
            syncOutcome = SyncOutcome.Success(namesLoaded = 5)
        )

        viewModel.events.test {
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun emits_AutoRefreshFailed_when_the_periodic_check_fails() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(
            lastRefreshTimestamp = null, // never refreshed, so a refresh is due
            syncOutcome = SyncOutcome.Error(SyncFailureReason.NETWORK)
        )

        viewModel.events.test {
            runCurrent()
            assertEquals(NameListEvent.AutoRefreshFailed, awaitItem())
        }
    }

    private fun createViewModel(
        names: List<NameEntry> = allNames,
        lastRefreshTimestamp: Long? = fixedNow,
        syncOutcome: SyncOutcome = SyncOutcome.Success(namesLoaded = 5)
    ): NameListViewModel {
        val nameRepository = FakeNameRepository(names)
        val settingsRepository = FakeSettingsRepository(lastRefreshTimestamp = lastRefreshTimestamp)
        val syncRepository = FakeNameSyncRepository(syncOutcome)
        val clock = { fixedNow }
        return NameListViewModel(
            observeNamesUseCase = ObserveNamesUseCase(nameRepository),
            observeNameCountUseCase = ObserveNameCountUseCase(nameRepository),
            refreshNamesIfDueUseCase = RefreshNamesIfDueUseCase(
                settingsRepository = settingsRepository,
                syncNamesUseCase = SyncNamesUseCase(syncRepository, settingsRepository, clock),
                currentTimeMillis = clock
            ),
            observeSearchEngineUseCase = ObserveSearchEngineUseCase(settingsRepository)
        )
    }
}
