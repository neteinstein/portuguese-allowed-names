package org.neteinstein.pickaname.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.repository.SettingsRepository

class ObserveSourceUrlUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val useCase = ObserveSourceUrlUseCase(settingsRepository)

    @Test
    fun `emits whatever the settings repository observes`() = runTest {
        every { settingsRepository.observeSourceUrl() } returns flowOf("https://example.com/list.pdf")

        useCase().test {
            assertThat(awaitItem()).isEqualTo("https://example.com/list.pdf")
            awaitComplete()
        }
        verify(exactly = 1) { settingsRepository.observeSourceUrl() }
    }
}
