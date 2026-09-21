package org.neteinstein.pickaname.presentation.settings

import app.cash.turbine.test
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
import org.neteinstein.pickaname.fake.FakeSettingsRepository
import org.neteinstein.pickaname.util.MainDispatcherHarness
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * In `commonTest`, so it runs on Android, wasmJs and iOS. Real use cases over a fake repository
 * rather than mocked use cases: the assertions become "the setting was actually persisted"
 * instead of "a mock was called", and it works without MockK, which is JVM-only.
 */
class SettingsViewModelTest {

    private val mainDispatcher = MainDispatcherHarness()

    @BeforeTest
    fun installMainDispatcher() = mainDispatcher.install()

    @AfterTest
    fun uninstallMainDispatcher() = mainDispatcher.uninstall()

    private val currentUrl = "https://current.example.com/list.pdf"

    @Test
    fun loads_the_currently_configured_url_on_init() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()

        assertEquals(currentUrl, viewModel.uiState.value.sourceUrl)
        assertFalse(viewModel.uiState.value.urlError)
    }

    @Test
    fun loads_the_currently_configured_refresh_period_on_init() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(refreshPeriod = RefreshPeriod.QUARTERLY)
        runCurrent()

        assertEquals(RefreshPeriod.QUARTERLY, viewModel.uiState.value.refreshPeriod)
    }

    @Test
    fun defaults_the_refresh_period_to_yearly_before_it_loads() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel()

        assertEquals(RefreshPeriod.YEARLY, viewModel.uiState.value.refreshPeriod)
    }

    @Test
    fun loads_the_currently_configured_search_engine_on_init() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(searchEngine = SearchEngine.GOOGLE)
        runCurrent()

        assertEquals(SearchEngine.GOOGLE, viewModel.uiState.value.searchEngine)
    }

    @Test
    fun defaults_the_search_engine_to_brave_before_it_loads() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel()

        assertEquals(SearchEngine.BRAVE, viewModel.uiState.value.searchEngine)
    }

    @Test
    fun loads_the_last_refresh_timestamp_on_init() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(lastRefreshTimestamp = 1_700_000_000_000L)
        runCurrent()

        assertEquals(1_700_000_000_000L, viewModel.uiState.value.lastRefreshTimestamp)
    }

    @Test
    fun selecting_a_search_engine_updates_state_immediately_and_persists_it() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.onSearchEngineSelected(SearchEngine.GOOGLE)

        assertEquals(SearchEngine.GOOGLE, viewModel.uiState.value.searchEngine)
        runCurrent()
        assertEquals(SearchEngine.GOOGLE, repository.getSearchEngine())
    }

    @Test
    fun saving_a_valid_url_clears_any_error_and_emits_a_SourceUpdated_event() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.events.test {
            viewModel.onUrlChange("https://new.example.com/list.pdf")
            viewModel.onSave()
            runCurrent()

            assertEquals(SettingsEvent.SourceUpdated, awaitItem())
        }
        assertFalse(viewModel.uiState.value.urlError)
        assertEquals("https://new.example.com/list.pdf", repository.getSourceUrl())
    }

    @Test
    fun saving_an_invalid_url_surfaces_an_error_and_does_not_emit_an_event() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.events.test {
            viewModel.onUrlChange("not a url")
            viewModel.onSave()
            runCurrent()

            expectNoEvents()
        }
        assertTrue(viewModel.uiState.value.urlError)
        // Still the old value: an invalid URL must not be persisted.
        assertEquals(currentUrl, repository.getSourceUrl())
    }

    @Test
    fun changing_the_url_clears_a_previous_error() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel()
        runCurrent()
        viewModel.onUrlChange("not a url")
        viewModel.onSave()
        runCurrent()
        assertTrue(viewModel.uiState.value.urlError)

        viewModel.onUrlChange("https://fixed.example.com/list.pdf")

        assertFalse(viewModel.uiState.value.urlError)
    }

    @Test
    fun resetting_restores_the_default_url_and_emits_a_SourceUpdated_event() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.events.test {
            viewModel.onReset()
            runCurrent()

            assertEquals(SettingsEvent.SourceUpdated, awaitItem())
        }
        assertEquals(NamesSourceDefaults.DEFAULT_SOURCE_URL, viewModel.uiState.value.sourceUrl)
        assertEquals(NamesSourceDefaults.DEFAULT_SOURCE_URL, repository.getSourceUrl())
    }

    @Test
    fun selecting_a_refresh_period_updates_state_immediately_and_persists_it() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)
        val viewModel = createViewModel(repository)
        runCurrent()

        viewModel.onRefreshPeriodSelected(RefreshPeriod.WEEKLY)

        assertEquals(RefreshPeriod.WEEKLY, viewModel.uiState.value.refreshPeriod)
        runCurrent()
        assertEquals(RefreshPeriod.WEEKLY, repository.getRefreshPeriod())
    }

    private fun createViewModel(
        refreshPeriod: RefreshPeriod = RefreshPeriod.DEFAULT,
        searchEngine: SearchEngine = SearchEngine.DEFAULT,
        lastRefreshTimestamp: Long? = null
    ): SettingsViewModel = createViewModel(
        FakeSettingsRepository(
            sourceUrl = currentUrl,
            refreshPeriod = refreshPeriod,
            searchEngine = searchEngine,
            lastRefreshTimestamp = lastRefreshTimestamp
        )
    )

    private fun createViewModel(repository: FakeSettingsRepository): SettingsViewModel =
        SettingsViewModel(
            getSourceUrlUseCase = GetSourceUrlUseCase(repository),
            updateSourceUrlUseCase = UpdateSourceUrlUseCase(repository),
            resetSourceUrlUseCase = ResetSourceUrlUseCase(repository),
            getRefreshPeriodUseCase = GetRefreshPeriodUseCase(repository),
            getLastRefreshTimestampUseCase = GetLastRefreshTimestampUseCase(repository),
            updateRefreshPeriodUseCase = UpdateRefreshPeriodUseCase(repository),
            getSearchEngineUseCase = GetSearchEngineUseCase(repository),
            updateSearchEngineUseCase = UpdateSearchEngineUseCase(repository)
        )
}
