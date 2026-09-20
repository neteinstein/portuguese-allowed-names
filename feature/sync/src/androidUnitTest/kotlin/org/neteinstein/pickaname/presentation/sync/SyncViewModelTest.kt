package org.neteinstein.pickaname.presentation.sync

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.usecase.GetSourceUrlUseCase
import org.neteinstein.pickaname.domain.usecase.SyncNamesUseCase
import org.neteinstein.pickaname.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class SyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getSourceUrlUseCase: GetSourceUrlUseCase = mockk()
    private val syncNamesUseCase: SyncNamesUseCase = mockk()
    private val url = "https://example.com/list.pdf"

    @Test
    fun `starts in the loading state before the sync completes`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { getSourceUrlUseCase() } returns url
        coEvery { syncNamesUseCase(any()) } returns SyncOutcome.Success(10)

        val viewModel = SyncViewModel(getSourceUrlUseCase, syncNamesUseCase)

        assertThat(viewModel.uiState.value).isEqualTo(SyncUiState.Loading)
    }

    @Test
    fun `transitions to success once the sync completes`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { getSourceUrlUseCase() } returns url
        coEvery { syncNamesUseCase(url) } returns SyncOutcome.Success(10)

        val viewModel = SyncViewModel(getSourceUrlUseCase, syncNamesUseCase)
        runCurrent()

        assertThat(viewModel.uiState.value).isEqualTo(SyncUiState.Success)
    }

    @Test
    fun `transitions to an error state when the sync fails`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { getSourceUrlUseCase() } returns url
        coEvery { syncNamesUseCase(url) } returns SyncOutcome.Error(SyncFailureReason.NETWORK)

        val viewModel = SyncViewModel(getSourceUrlUseCase, syncNamesUseCase)
        runCurrent()

        assertThat(viewModel.uiState.value).isEqualTo(SyncUiState.Error(SyncFailureReason.NETWORK))
    }

    @Test
    fun `retry re-runs the sync and can succeed after a prior failure`() = runTest(mainDispatcherRule.dispatcher) {
        coEvery { getSourceUrlUseCase() } returns url
        coEvery { syncNamesUseCase(url) } returnsMany listOf(
            SyncOutcome.Error(SyncFailureReason.NETWORK),
            SyncOutcome.Success(10)
        )

        val viewModel = SyncViewModel(getSourceUrlUseCase, syncNamesUseCase)
        runCurrent()
        assertThat(viewModel.uiState.value).isEqualTo(SyncUiState.Error(SyncFailureReason.NETWORK))

        viewModel.retry()
        assertThat(viewModel.uiState.value).isEqualTo(SyncUiState.Loading)
        runCurrent()

        assertThat(viewModel.uiState.value).isEqualTo(SyncUiState.Success)
        coVerify(exactly = 2) { syncNamesUseCase(url) }
    }
}
