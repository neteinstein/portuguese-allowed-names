package org.neteinstein.pickaname.presentation.namelist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.neteinstein.pickaname.domain.model.AutoRefreshResult
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.TraditionalNameRules
import org.neteinstein.pickaname.domain.usecase.ObserveNameCountUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveNamesUseCase
import org.neteinstein.pickaname.domain.usecase.ObserveSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.RefreshNamesIfDueUseCase
import org.neteinstein.pickaname.util.MainDispatcherRule

/**
 * [NameListViewModel.uiState] is backed by `stateIn(..., NameListUiState())`, so the very first
 * item any collector sees is always that static initial value - before the real (mocked) data
 * has had a chance to flow through `combine`/`flatMapLatest`. Every test below accounts for that
 * by awaiting it explicitly, then calling `runCurrent()` to let the real pipeline compute its
 * first genuine result, before asserting on anything.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NameListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alice = NameEntry(1, "Alice", Gender.FEMALE)
    private val bob = NameEntry(2, "Bob", Gender.MALE)
    private val kevin = NameEntry(3, "Kevin", Gender.MALE)
    private val allNames = listOf(alice, bob)

    private fun matches(entry: NameEntry, filter: NameFilter): Boolean =
        (filter.gender == null || entry.gender == filter.gender) &&
            (filter.initial == null || entry.name.first().uppercaseChar() == filter.initial) &&
            (filter.query.isBlank() || entry.name.contains(filter.query, ignoreCase = true)) &&
            (!filter.traditionalOnly || TraditionalNameRules.isTraditional(entry.name))

    private val observeNamesUseCase: ObserveNamesUseCase = mockk()
    private val observeNameCountUseCase: ObserveNameCountUseCase = mockk()
    private val refreshNamesIfDueUseCase: RefreshNamesIfDueUseCase = mockk()
    private val observeSearchEngineUseCase: ObserveSearchEngineUseCase = mockk()

    private fun createViewModel(names: List<NameEntry> = allNames): NameListViewModel {
        every { observeNamesUseCase(any()) } answers {
            val filter = firstArg<NameFilter>()
            flowOf(names.filter { matches(it, filter) })
        }
        every { observeNameCountUseCase(any()) } answers {
            val filter = firstArg<NameFilter>()
            flowOf(names.count { matches(it, filter) })
        }
        coEvery { refreshNamesIfDueUseCase() } returns AutoRefreshResult.NotDue
        every { observeSearchEngineUseCase() } returns flowOf(SearchEngine.DEFAULT)
        return NameListViewModel(
            observeNamesUseCase,
            observeNameCountUseCase,
            refreshNamesIfDueUseCase,
            observeSearchEngineUseCase
        )
    }

    @Test
    fun `initial state exposes the unfiltered list and total count`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // stateIn's static default, emitted before the pipeline has run at all
            runCurrent()

            val state = awaitItem()
            assertThat(state.names).containsExactly(alice, bob)
            assertThat(state.count).isEqualTo(2)
            assertThat(state.selectedGender).isNull()
            assertThat(state.selectedInitial).isNull()
        }
    }

    @Test
    fun `selecting a gender filters immediately without waiting for debounce`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // static default
            runCurrent()
            awaitItem() // real unfiltered first result

            viewModel.onGenderSelected(Gender.MALE)
            runCurrent()

            val state = awaitItem()
            assertThat(state.names).containsExactly(bob)
            assertThat(state.count).isEqualTo(1)
            assertThat(state.selectedGender).isEqualTo(Gender.MALE)
        }
    }

    @Test
    fun `selecting an initial letter filters immediately`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // static default
            runCurrent()
            awaitItem() // real unfiltered first result

            viewModel.onInitialSelected('B')
            runCurrent()

            val state = awaitItem()
            assertThat(state.names).containsExactly(bob)
            assertThat(state.selectedInitial).isEqualTo('B')
        }
    }

    @Test
    fun `enabling traditionalOnly excludes names that fail the traditional-names heuristic`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel(names = listOf(alice, bob, kevin))

        viewModel.uiState.test {
            awaitItem() // static default
            runCurrent()
            awaitItem() // real unfiltered first result

            viewModel.onTraditionalOnlyChanged(true)
            runCurrent()

            val state = awaitItem()
            assertThat(state.names).containsExactly(alice, bob)
            assertThat(state.count).isEqualTo(2)
            assertThat(state.traditionalOnly).isTrue()
        }
    }

    @Test
    fun `a gender selection made before the first result arrives still applies immediately`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // static default

            // Selecting a filter before the pipeline has produced its first real value at all
            // (e.g. a very fast tap right as the screen opens) must not be lost, delayed, or
            // preceded by a transient unfiltered result.
            viewModel.onGenderSelected(Gender.MALE)
            runCurrent()

            val state = awaitItem()
            assertThat(state.names).containsExactly(bob)
            assertThat(state.selectedGender).isEqualTo(Gender.MALE)
        }
    }

    @Test
    fun `typing a query echoes immediately but results wait for the debounce window`() = runTest(mainDispatcherRule.dispatcher) {
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
            assertThat(echoed.query).isEqualTo("bob")
            assertThat(echoed.names).containsExactly(alice, bob)

            advanceTimeBy(300)
            runCurrent()

            val state = awaitItem()
            assertThat(state.names).containsExactly(bob)
            assertThat(state.query).isEqualTo("bob")
        }
    }

    @Test
    fun `rapid query changes echo every keystroke but only refilter once the debounce settles`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // static default
            runCurrent()
            awaitItem() // real unfiltered first result

            viewModel.onQueryChange("a")
            runCurrent()
            assertThat(awaitItem().query).isEqualTo("a")

            advanceTimeBy(100)
            viewModel.onQueryChange("al")
            runCurrent()
            assertThat(awaitItem().query).isEqualTo("al")

            advanceTimeBy(100)
            viewModel.onQueryChange("ali")
            runCurrent()
            val lastEcho = awaitItem()
            assertThat(lastEcho.query).isEqualTo("ali")
            assertThat(lastEcho.names).containsExactly(alice, bob) // not refiltered yet

            advanceTimeBy(300)
            runCurrent()

            val state = awaitItem()
            assertThat(state.query).isEqualTo("ali")
            assertThat(state.names).containsExactly(alice)
        }
    }

    @Test
    fun `does not emit an event when no auto-refresh is due`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        viewModel.events.test {
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun `does not emit an event when the auto-refresh succeeds`() = runTest(mainDispatcherRule.dispatcher) {
        every { observeNamesUseCase(any()) } answers {
            flowOf(allNames.filter { matches(it, firstArg()) })
        }
        every { observeNameCountUseCase(any()) } answers {
            flowOf(allNames.count { matches(it, firstArg()) })
        }
        coEvery { refreshNamesIfDueUseCase() } returns AutoRefreshResult.Refreshed(namesLoaded = 5)
        every { observeSearchEngineUseCase() } returns flowOf(SearchEngine.DEFAULT)
        val viewModel = NameListViewModel(
            observeNamesUseCase,
            observeNameCountUseCase,
            refreshNamesIfDueUseCase,
            observeSearchEngineUseCase
        )

        viewModel.events.test {
            runCurrent()
            expectNoEvents()
        }
    }

    @Test
    fun `emits AutoRefreshFailed when the periodic check fails`() = runTest(mainDispatcherRule.dispatcher) {
        every { observeNamesUseCase(any()) } answers {
            flowOf(allNames.filter { matches(it, firstArg()) })
        }
        every { observeNameCountUseCase(any()) } answers {
            flowOf(allNames.count { matches(it, firstArg()) })
        }
        coEvery { refreshNamesIfDueUseCase() } returns AutoRefreshResult.Failed(SyncFailureReason.NETWORK)
        every { observeSearchEngineUseCase() } returns flowOf(SearchEngine.DEFAULT)
        val viewModel = NameListViewModel(
            observeNamesUseCase,
            observeNameCountUseCase,
            refreshNamesIfDueUseCase,
            observeSearchEngineUseCase
        )

        viewModel.events.test {
            runCurrent()
            assertThat(awaitItem()).isEqualTo(NameListEvent.AutoRefreshFailed)
        }
    }
}
