package org.neteinstein.pickaname.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.local.database.NameRecord
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameEntry
import org.neteinstein.pickaname.domain.model.NameFilter

class NameRepositoryImplTest {

    private val nameLocalDataSource: NameLocalDataSource = mockk()
    private val repository = NameRepositoryImpl(nameLocalDataSource)

    private val aliceRecord = NameRecord(id = 1, name = "Alice", gender = "F", initialLetter = "A")
    private val aliceEntry = NameEntry(id = 1, name = "Alice", gender = Gender.FEMALE)
    private val kevinRecord = NameRecord(id = 2, name = "Kevin", gender = "M", initialLetter = "K")
    private val kevinEntry = NameEntry(id = 2, name = "Kevin", gender = Gender.MALE)

    // Passes every phonotactic check and isn't in the curated allowlist, so it's traditional
    // unless something else (like being used by both genders) says otherwise.
    private val arianaRecord = NameRecord(id = 3, name = "Ariana", gender = "F", initialLetter = "A")

    @Test
    fun `observeNames passes an unrestricted filter through as all-null dao params`() = runTest {
        every { nameLocalDataSource.observeNames(gender = null, initial = null, query = null) } returns
            flowOf(listOf(aliceRecord))

        repository.observeNames(NameFilter()).test {
            assertThat(awaitItem()).containsExactly(aliceEntry)
            awaitComplete()
        }
    }

    @Test
    fun `observeNames maps gender to its entity code and trims the query`() = runTest {
        every { nameLocalDataSource.observeNames(gender = "F", initial = null, query = "ali") } returns
            flowOf(listOf(aliceRecord))

        val filter = NameFilter(query = "  ali  ", gender = Gender.FEMALE)

        repository.observeNames(filter).test {
            assertThat(awaitItem()).containsExactly(aliceEntry)
            awaitComplete()
        }
    }

    @Test
    fun `observeNames strips diacritics and upper-cases the initial before querying`() = runTest {
        every { nameLocalDataSource.observeNames(gender = null, initial = "A", query = null) } returns
            flowOf(listOf(aliceRecord))

        repository.observeNames(NameFilter(initial = 'á')).test {
            assertThat(awaitItem()).containsExactly(aliceEntry)
            awaitComplete()
        }
    }

    @Test
    fun `observeNames treats a blank query as no query restriction`() = runTest {
        every { nameLocalDataSource.observeNames(gender = null, initial = null, query = null) } returns
            flowOf(emptyList())

        repository.observeNames(NameFilter(query = "   ")).test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `observeNames excludes non-traditional names when traditionalOnly is enabled`() = runTest {
        every { nameLocalDataSource.observeNames(gender = null, initial = null, query = null) } returns
            flowOf(listOf(aliceRecord, kevinRecord))
        every { nameLocalDataSource.observeNamesUsedByBothGenders() } returns flowOf(emptyList())

        repository.observeNames(NameFilter(traditionalOnly = true)).test {
            assertThat(awaitItem()).containsExactly(aliceEntry)
            awaitComplete()
        }
    }

    @Test
    fun `observeNames excludes a name used by both genders when traditionalOnly is enabled`() = runTest {
        every { nameLocalDataSource.observeNames(gender = null, initial = null, query = null) } returns
            flowOf(listOf(arianaRecord))
        every { nameLocalDataSource.observeNamesUsedByBothGenders() } returns flowOf(listOf("Ariana"))

        repository.observeNames(NameFilter(traditionalOnly = true)).test {
            assertThat(awaitItem()).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `observeNames keeps a curated name traditional even if used by both genders`() = runTest {
        every { nameLocalDataSource.observeNames(gender = null, initial = null, query = null) } returns
            flowOf(listOf(aliceRecord))
        every { nameLocalDataSource.observeNamesUsedByBothGenders() } returns flowOf(listOf("Alice"))

        repository.observeNames(NameFilter(traditionalOnly = true)).test {
            assertThat(awaitItem()).containsExactly(aliceEntry)
            awaitComplete()
        }
    }

    @Test
    fun `observeNames keeps all names and never queries both-genders names when traditionalOnly is disabled`() =
        runTest {
            every { nameLocalDataSource.observeNames(gender = null, initial = null, query = null) } returns
                flowOf(listOf(aliceRecord, kevinRecord))

            repository.observeNames(NameFilter(traditionalOnly = false)).test {
                assertThat(awaitItem()).containsExactly(aliceEntry, kevinEntry)
                awaitComplete()
            }

            verify(exactly = 0) { nameLocalDataSource.observeNamesUsedByBothGenders() }
        }

    @Test
    fun `observeNameCount derives its count from the traditional-only filtered list`() = runTest {
        every { nameLocalDataSource.observeNames(gender = null, initial = null, query = null) } returns
            flowOf(listOf(aliceRecord, kevinRecord))
        every { nameLocalDataSource.observeNamesUsedByBothGenders() } returns flowOf(emptyList())

        repository.observeNameCount(NameFilter(traditionalOnly = true)).test {
            assertThat(awaitItem()).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun `observeNameCount applies the same filter mapping as observeNames`() = runTest {
        every { nameLocalDataSource.observeCount(gender = "M", initial = null, query = "bob") } returns flowOf(1)

        val filter = NameFilter(query = "bob", gender = Gender.MALE)

        repository.observeNameCount(filter).test {
            assertThat(awaitItem()).isEqualTo(1)
            awaitComplete()
        }
    }

    @Test
    fun `observeIsEmpty is true only when the total row count is zero`() = runTest {
        every { nameLocalDataSource.observeTotalCount() } returns flowOf(0)

        repository.observeIsEmpty().test {
            assertThat(awaitItem()).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `observeIsEmpty is false once there is at least one row`() = runTest {
        every { nameLocalDataSource.observeTotalCount() } returns flowOf(5)

        repository.observeIsEmpty().test {
            assertThat(awaitItem()).isFalse()
            awaitComplete()
        }
    }
}
