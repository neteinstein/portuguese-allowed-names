package org.neteinstein.pickaname.presentation.sync

import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.usecase.GetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.SyncNamesUseCase
import org.neteinstein.pickaname.fake.FakeNameSyncRepository
import org.neteinstein.pickaname.fake.FakeSettingsRepository
import org.neteinstein.pickaname.util.MainDispatcherHarness
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * In `commonTest`, so it runs on Android, wasmJs and iOS. The use cases are real (they're small
 * and final); only the repositories under them are faked - which also means this covers the
 * use-case wiring, not just the ViewModel.
 */
class SyncViewModelTest {

    private val mainDispatcher = MainDispatcherHarness()

    @BeforeTest
    fun installMainDispatcher() = mainDispatcher.install()

    @AfterTest
    fun uninstallMainDispatcher() = mainDispatcher.uninstall()

    private val url = "https://example.com/list.pdf"

    @Test
    fun starts_in_the_loading_state_before_the_sync_completes() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(SyncOutcome.Success(10))

        assertEquals(SyncUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun transitions_to_success_once_the_sync_completes() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(SyncOutcome.Success(10))

        runCurrent()

        assertEquals(SyncUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun transitions_to_an_error_state_when_the_sync_fails() = runTest(mainDispatcher.dispatcher) {
        val viewModel = createViewModel(SyncOutcome.Error(SyncFailureReason.NETWORK))

        runCurrent()

        assertEquals(SyncUiState.Error(SyncFailureReason.NETWORK), viewModel.uiState.value)
    }

    @Test
    fun retry_re_runs_the_sync_and_can_succeed_after_a_prior_failure() = runTest(mainDispatcher.dispatcher) {
        val syncRepository = FakeNameSyncRepository(
            SyncOutcome.Error(SyncFailureReason.NETWORK),
            SyncOutcome.Success(10)
        )
        val viewModel = createViewModel(syncRepository)

        runCurrent()
        assertEquals(SyncUiState.Error(SyncFailureReason.NETWORK), viewModel.uiState.value)

        viewModel.retry()
        assertEquals(SyncUiState.Loading, viewModel.uiState.value)
        runCurrent()

        assertEquals(SyncUiState.Success, viewModel.uiState.value)
        assertEquals(listOf(url, url), syncRepository.syncedUrls)
    }

    private fun createViewModel(vararg outcomes: SyncOutcome): SyncViewModel =
        createViewModel(FakeNameSyncRepository(*outcomes))

    private fun createViewModel(syncRepository: FakeNameSyncRepository): SyncViewModel {
        val settingsRepository = FakeSettingsRepository(sourceUrl = url)
        return SyncViewModel(
            getSourceUrlUseCase = GetSourceUrlUseCase(settingsRepository),
            syncNamesUseCase = SyncNamesUseCase(syncRepository, settingsRepository)
        )
    }
}
