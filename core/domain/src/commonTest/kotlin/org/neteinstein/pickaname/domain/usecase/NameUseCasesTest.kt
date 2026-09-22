package org.neteinstein.pickaname.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter
import org.neteinstein.pickaname.fake.FakeNameRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The names-facing use cases, in `commonTest` so they run on every platform. */
class NameUseCasesTest {

    private val alice = NameEntry(id = 1, name = "Alice", gender = Gender.FEMALE)
    private val bruno = NameEntry(id = 2, name = "Bruno", gender = Gender.MALE)

    @Test
    fun observe_names_passes_the_filter_through_and_emits_matches() = runTest {
        val repository = FakeNameRepository(listOf(alice, bruno))
        val filter = NameFilter(gender = Gender.FEMALE)

        ObserveNamesUseCase(repository)(filter).test {
            assertEquals(listOf(alice), awaitItem())
        }
        assertEquals(filter, repository.lastFilter)
    }

    @Test
    fun observe_name_count_counts_the_same_matches() = runTest {
        val repository = FakeNameRepository(listOf(alice, bruno))

        ObserveNameCountUseCase(repository)(NameFilter()).test {
            assertEquals(2, awaitItem())
        }
    }

    @Test
    fun needs_initial_sync_is_true_only_while_the_list_is_empty() = runTest {
        val repository = FakeNameRepository(emptyList())
        val useCase = ObserveNeedsInitialSyncUseCase(repository)

        useCase().test {
            assertTrue(awaitItem())

            repository.setNames(listOf(alice))
            assertFalse(awaitItem())
        }
    }
}
