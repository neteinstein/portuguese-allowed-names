package org.neteinstein.pickaname.presentation.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.neteinstein.pickaname.domain.model.NamesSourceDefaults
import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.model.SearchEngine
import org.neteinstein.pickaname.domain.usecase.GetLastRefreshTimestampUseCase
import org.neteinstein.pickaname.domain.usecase.GetRefreshPeriodUseCase
import org.neteinstein.pickaname.domain.usecase.GetSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.GetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.ResetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateRefreshPeriodUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateSearchEngineUseCase
import org.neteinstein.pickaname.domain.usecase.UpdateSourceUrlUseCase
import org.neteinstein.pickaname.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSourceUrlUseCase: GetSourceUrlUseCase = mockk()
    private val updateSourceUrlUseCase: UpdateSourceUrlUseCase = mockk()
    private val resetSourceUrlUseCase: ResetSourceUrlUseCase = mockk()
    private val getRefreshPeriodUseCase: GetRefreshPeriodUseCase = mockk()
    private val getLastRefreshTimestampUseCase: GetLastRefreshTimestampUseCase = mockk()
    private val updateRefreshPeriodUseCase: UpdateRefreshPeriodUseCase = mockk(relaxed = true)
    private val getSearchEngineUseCase: GetSearchEngineUseCase = mockk()
    private val updateSearchEngineUseCase: UpdateSearchEngineUseCase = mockk(relaxed = true)

    private fun createViewModel(
        refreshPeriod: RefreshPeriod = RefreshPeriod.DEFAULT,
        searchEngine: SearchEngine = SearchEngine.DEFAULT,
        lastRefreshTimestamp: Long? = null
    ): SettingsViewModel {
        coEvery { getSourceUrlUseCase() } returns "https://current.example.com/list.pdf"
        coEvery { getRefreshPeriodUseCase() } returns refreshPeriod
        coEvery { getSearchEngineUseCase() } returns searchEngine
        coEvery { getLastRefreshTimestampUseCase() } returns lastRefreshTimestamp
        return SettingsViewModel(
            getSourceUrlUseCase,
            updateSourceUrlUseCase,
            resetSourceUrlUseCase,
            getRefreshPeriodUseCase,
            getLastRefreshTimestampUseCase,
            updateRefreshPeriodUseCase,
            getSearchEngineUseCase,
            updateSearchEngineUseCase
        )
    }

    @Test
    fun `loads the currently configured url on init`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        assertThat(viewModel.uiState.value.sourceUrl).isEqualTo("https://current.example.com/list.pdf")
        assertThat(viewModel.uiState.value.urlError).isFalse()
    }

    @Test
    fun `loads the currently configured refresh period on init`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel(refreshPeriod = RefreshPeriod.QUARTERLY)
        runCurrent()

        assertThat(viewModel.uiState.value.refreshPeriod).isEqualTo(RefreshPeriod.QUARTERLY)
    }

    @Test
    fun `defaults the refresh period to yearly before it loads`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.refreshPeriod).isEqualTo(RefreshPeriod.YEARLY)
    }

    @Test
    fun `loads the currently configured search engine on init`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel(searchEngine = SearchEngine.GOOGLE)
        runCurrent()

        assertThat(viewModel.uiState.value.searchEngine).isEqualTo(SearchEngine.GOOGLE)
    }

    @Test
    fun `defaults the search engine to brave before it loads`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.searchEngine).isEqualTo(SearchEngine.BRAVE)
    }

    @Test
    fun `selecting a search engine updates state immediately and persists it`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onSearchEngineSelected(SearchEngine.GOOGLE)

        assertThat(viewModel.uiState.value.searchEngine).isEqualTo(SearchEngine.GOOGLE)
        runCurrent()
        coVerify(exactly = 1) { updateSearchEngineUseCase(SearchEngine.GOOGLE) }
    }

    @Test
    fun `saving a valid url clears any error and emits a SourceUpdated event`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        coEvery { updateSourceUrlUseCase(any()) } returns Result.success(Unit)

        viewModel.events.test {
            viewModel.onUrlChange("https://new.example.com/list.pdf")
            viewModel.onSave()
            runCurrent()

            assertThat(awaitItem()).isEqualTo(SettingsEvent.SourceUpdated)
        }
        assertThat(viewModel.uiState.value.urlError).isFalse()
        coVerify(exactly = 1) { updateSourceUrlUseCase("https://new.example.com/list.pdf") }
    }

    @Test
    fun `saving an invalid url surfaces an error and does not emit an event`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        coEvery { updateSourceUrlUseCase(any()) } returns Result.failure(IllegalArgumentException("bad url"))

        viewModel.events.test {
            viewModel.onUrlChange("not a url")
            viewModel.onSave()
            runCurrent()

            expectNoEvents()
        }
        assertThat(viewModel.uiState.value.urlError).isTrue()
    }

    @Test
    fun `changing the url clears a previous error`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        coEvery { updateSourceUrlUseCase(any()) } returns Result.failure(IllegalArgumentException("bad url"))
        viewModel.onUrlChange("not a url")
        viewModel.onSave()
        runCurrent()
        assertThat(viewModel.uiState.value.urlError).isTrue()

        viewModel.onUrlChange("https://fixed.example.com/list.pdf")

        assertThat(viewModel.uiState.value.urlError).isFalse()
    }

    @Test
    fun `resetting restores the default url and emits a SourceUpdated event`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        coEvery { resetSourceUrlUseCase() } returns Unit

        viewModel.events.test {
            viewModel.onReset()
            runCurrent()

            assertThat(awaitItem()).isEqualTo(SettingsEvent.SourceUpdated)
        }
        assertThat(viewModel.uiState.value.sourceUrl).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)
        coVerify(exactly = 1) { resetSourceUrlUseCase() }
    }

    @Test
    fun `selecting a refresh period updates state immediately and persists it`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        viewModel.onRefreshPeriodSelected(RefreshPeriod.WEEKLY)

        assertThat(viewModel.uiState.value.refreshPeriod).isEqualTo(RefreshPeriod.WEEKLY)
        runCurrent()
        coVerify(exactly = 1) { updateRefreshPeriodUseCase(RefreshPeriod.WEEKLY) }
    }
}
