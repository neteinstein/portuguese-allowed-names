package org.neteinstein.pickaname.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.repository.NameRepository

class ObserveNameCountUseCaseTest {

    private val nameRepository: NameRepository = mockk()
    private val useCase = ObserveNameCountUseCase(nameRepository)

    @Test
    fun `forwards the filter to the repository and emits its count`() = runTest {
        val filter = NameFilter(gender = Gender.MALE)
        every { nameRepository.observeNameCount(filter) } returns flowOf(3)

        useCase(filter).test {
            assertThat(awaitItem()).isEqualTo(3)
            awaitComplete()
        }
        verify(exactly = 1) { nameRepository.observeNameCount(filter) }
    }
}
