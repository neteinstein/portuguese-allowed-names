package org.neteinstein.pickaname.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.repository.NameSyncRepository
import org.neteinstein.pickaname.domain.repository.SettingsRepository

class SyncNamesUseCaseTest {

    private val nameSyncRepository: NameSyncRepository = mockk()
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val fixedNow = 1_700_000_000_000L

    private val useCase = SyncNamesUseCase(
        nameSyncRepository = nameSyncRepository,
        settingsRepository = settingsRepository,
        currentTimeMillis = { fixedNow }
    )

    private val url = "https://example.com/list.pdf"

    @Test
    fun `stamps the last-refresh timestamp when the sync succeeds`() = runTest {
        coEvery { nameSyncRepository.syncFromUrl(url) } returns SyncOutcome.Success(namesLoaded = 10)

        val outcome = useCase(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Success(namesLoaded = 10))
        coVerify(exactly = 1) { settingsRepository.setLastRefreshTimestamp(fixedNow) }
    }

    @Test
    fun `does not stamp the last-refresh timestamp when the sync fails`() = runTest {
        coEvery { nameSyncRepository.syncFromUrl(url) } returns SyncOutcome.Error(SyncFailureReason.NETWORK)

        val outcome = useCase(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Error(SyncFailureReason.NETWORK))
        coVerify(exactly = 0) { settingsRepository.setLastRefreshTimestamp(any()) }
    }
}
