package org.neteinstein.pickaname.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.model.AutoRefreshResult
import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.repository.SettingsRepository

class RefreshNamesIfDueUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val syncNamesUseCase: SyncNamesUseCase = mockk()
    private val fixedNow = 1_700_000_000_000L
    private val url = "https://example.com/list.pdf"

    private val useCase = RefreshNamesIfDueUseCase(
        settingsRepository = settingsRepository,
        syncNamesUseCase = syncNamesUseCase,
        currentTimeMillis = { fixedNow }
    )

    @Test
    fun `is due and syncs when there is no prior refresh timestamp`() = runTest {
        coEvery { settingsRepository.getLastRefreshTimestamp() } returns null
        coEvery { settingsRepository.getRefreshPeriod() } returns RefreshPeriod.YEARLY
        coEvery { settingsRepository.getSourceUrl() } returns url
        coEvery { syncNamesUseCase(url) } returns SyncOutcome.Success(namesLoaded = 42)

        val result = useCase()

        assertThat(result).isEqualTo(AutoRefreshResult.Refreshed(namesLoaded = 42))
        coVerify(exactly = 1) { syncNamesUseCase(url) }
    }

    @Test
    fun `is not due when the configured period has not elapsed yet`() = runTest {
        val period = RefreshPeriod.MONTHLY
        coEvery { settingsRepository.getLastRefreshTimestamp() } returns fixedNow - (period.durationMillis / 2)
        coEvery { settingsRepository.getRefreshPeriod() } returns period

        val result = useCase()

        assertThat(result).isEqualTo(AutoRefreshResult.NotDue)
        coVerify(exactly = 0) { syncNamesUseCase(any()) }
    }

    @Test
    fun `is due once the configured period has fully elapsed`() = runTest {
        val period = RefreshPeriod.WEEKLY
        coEvery { settingsRepository.getLastRefreshTimestamp() } returns fixedNow - period.durationMillis
        coEvery { settingsRepository.getRefreshPeriod() } returns period
        coEvery { settingsRepository.getSourceUrl() } returns url
        coEvery { syncNamesUseCase(url) } returns SyncOutcome.Success(namesLoaded = 3)

        val result = useCase()

        assertThat(result).isEqualTo(AutoRefreshResult.Refreshed(namesLoaded = 3))
    }

    @Test
    fun `surfaces a failure without touching the database when the sync fails`() = runTest {
        coEvery { settingsRepository.getLastRefreshTimestamp() } returns null
        coEvery { settingsRepository.getRefreshPeriod() } returns RefreshPeriod.YEARLY
        coEvery { settingsRepository.getSourceUrl() } returns url
        coEvery { syncNamesUseCase(url) } returns SyncOutcome.Error(SyncFailureReason.NETWORK)

        val result = useCase()

        assertThat(result).isEqualTo(AutoRefreshResult.Failed(SyncFailureReason.NETWORK))
    }
}
