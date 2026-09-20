package org.neteinstein.pickaname.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.repository.SettingsRepository

class ResetSourceUrlUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val useCase = ResetSourceUrlUseCase(settingsRepository)

    @Test
    fun `delegates to the settings repository`() = runTest {
        coEvery { settingsRepository.resetSourceUrlToDefault() } returns Unit

        useCase()

        coVerify(exactly = 1) { settingsRepository.resetSourceUrlToDefault() }
    }
}
