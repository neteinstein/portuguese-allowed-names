package org.neteinstein.pickaname.presentation.splash

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.neteinstein.pickaname.domain.usecase.ObserveNeedsInitialSyncUseCase
import org.neteinstein.pickaname.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeNeedsInitialSyncUseCase: ObserveNeedsInitialSyncUseCase = mockk()

    @Test
    fun `navigates to sync when the database still needs an initial population`() = runTest(mainDispatcherRule.dispatcher) {
        every { observeNeedsInitialSyncUseCase() } returns flowOf(true)
        val viewModel = SplashViewModel(observeNeedsInitialSyncUseCase)

        advanceUntilIdle()

        assertThat(viewModel.destination.value).isEqualTo(SplashDestination.SYNC)
    }

    @Test
    fun `navigates to the name list when the database is already populated`() = runTest(mainDispatcherRule.dispatcher) {
        every { observeNeedsInitialSyncUseCase() } returns flowOf(false)
        val viewModel = SplashViewModel(observeNeedsInitialSyncUseCase)

        advanceUntilIdle()

        assertThat(viewModel.destination.value).isEqualTo(SplashDestination.NAME_LIST)
    }

    @Test
    fun `keeps showing the splash for the minimum duration even when the check resolves instantly`() = runTest(mainDispatcherRule.dispatcher) {
        every { observeNeedsInitialSyncUseCase() } returns flowOf(false)
        val viewModel = SplashViewModel(observeNeedsInitialSyncUseCase)

        runCurrent()
        assertThat(viewModel.destination.value).isNull()

        advanceUntilIdle()
        assertThat(viewModel.destination.value).isEqualTo(SplashDestination.NAME_LIST)
    }
}
