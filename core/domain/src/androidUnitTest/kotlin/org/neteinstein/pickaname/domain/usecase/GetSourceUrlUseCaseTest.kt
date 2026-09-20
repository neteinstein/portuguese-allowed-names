package org.neteinstein.pickaname.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.repository.SettingsRepository

class GetSourceUrlUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk()
    private val useCase = GetSourceUrlUseCase(settingsRepository)

    @Test
    fun `returns the url reported by the settings repository`() = runTest {
        coEvery { settingsRepository.getSourceUrl() } returns "https://example.com/list.pdf"

        val result = useCase()

        assertThat(result).isEqualTo("https://example.com/list.pdf")
        coVerify(exactly = 1) { settingsRepository.getSourceUrl() }
    }
}
