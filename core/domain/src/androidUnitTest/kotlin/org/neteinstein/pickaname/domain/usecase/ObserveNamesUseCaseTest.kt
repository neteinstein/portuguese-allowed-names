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
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.domain.repository.NameRepository

class ObserveNamesUseCaseTest {

    private val nameRepository: NameRepository = mockk()
    private val useCase = ObserveNamesUseCase(nameRepository)

    @Test
    fun `forwards the filter to the repository and emits its results`() = runTest {
        val filter = NameFilter(gender = Gender.FEMALE)
        val names = listOf(NameEntry(id = 1, name = "Alice", gender = Gender.FEMALE))
        every { nameRepository.observeNames(filter) } returns flowOf(names)

        useCase(filter).test {
            assertThat(awaitItem()).isEqualTo(names)
            awaitComplete()
        }
        verify(exactly = 1) { nameRepository.observeNames(filter) }
    }
}
