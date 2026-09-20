package org.neteinstein.pickaname.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.repository.NameRepository

class ObserveNeedsInitialSyncUseCaseTest {

    private val nameRepository: NameRepository = mockk()
    private val useCase = ObserveNeedsInitialSyncUseCase(nameRepository)

    @Test
    fun `emits true when the repository reports the names table is empty`() = runTest {
        every { nameRepository.observeIsEmpty() } returns flowOf(true)

        useCase().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `emits false when the repository reports the names table is populated`() = runTest {
        every { nameRepository.observeIsEmpty() } returns flowOf(false)

        useCase().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }
}
