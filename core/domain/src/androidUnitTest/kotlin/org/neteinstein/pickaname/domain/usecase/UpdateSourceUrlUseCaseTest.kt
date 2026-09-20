package org.neteinstein.pickaname.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.repository.SettingsRepository

class UpdateSourceUrlUseCaseTest {

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val useCase = UpdateSourceUrlUseCase(settingsRepository)

    @Test
    fun `persists a valid https url and trims surrounding whitespace`() = runTest {
        coEvery { settingsRepository.setSourceUrl(any()) } returns Unit

        val result = useCase("  https://example.com/list.pdf  ")

        assertThat(result.isSuccess).isTrue()
        coVerify(exactly = 1) { settingsRepository.setSourceUrl("https://example.com/list.pdf") }
    }

    @Test
    fun `accepts a valid http url`() = runTest {
        val result = useCase("http://example.com/list.pdf")

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `rejects a blank url`() = runTest {
        val result = useCase("   ")

        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { settingsRepository.setSourceUrl(any()) }
    }

    @Test
    fun `rejects a url with an unsupported scheme`() = runTest {
        val result = useCase("ftp://example.com/list.pdf")

        assertThat(result.isFailure).isTrue()
        coVerify(exactly = 0) { settingsRepository.setSourceUrl(any()) }
    }

    @Test
    fun `rejects a malformed url`() = runTest {
        val result = useCase("not a url at all")

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `rejects a url missing a host`() = runTest {
        val result = useCase("https://")

        assertThat(result.isFailure).isTrue()
    }
}
