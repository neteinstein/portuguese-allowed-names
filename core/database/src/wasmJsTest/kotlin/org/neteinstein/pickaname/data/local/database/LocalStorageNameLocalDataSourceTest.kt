package org.neteinstein.pickaname.data.local.database

import kotlinx.browser.localStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Runs in a real browser (`:core:database:wasmJsTest` → ChromeHeadless), against the real
 * `localStorage` - the point of these is precisely to check the web store behaves like the
 * Room/SQLite one it stands in for, so faking the storage away would defeat them.
 */
class LocalStorageNameLocalDataSourceTest {

    private val alice = NameRecord(name = "Alice", gender = "F", initialLetter = "A")
    private val alex = NameRecord(name = "Alex", gender = "M", initialLetter = "A")
    private val alexFemale = NameRecord(name = "Alex", gender = "F", initialLetter = "A")
    private val bruno = NameRecord(name = "Bruno", gender = "M", initialLetter = "B")

    @BeforeTest
    fun clearStorage() = localStorage.clear()

    @AfterTest
    fun cleanUpStorage() = localStorage.clear()

    @Test
    fun starts_empty_when_nothing_was_ever_stored() = runTest {
        val dataSource = LocalStorageNameLocalDataSource()

        assertEquals(0, dataSource.observeTotalCount().first())
        assertTrue(dataSource.observeNames(null, null, null).first().isEmpty())
    }

    @Test
    fun replaceAll_stores_names_sorted_case_insensitively_by_name_then_gender() = runTest {
        val dataSource = LocalStorageNameLocalDataSource()

        dataSource.replaceAll(listOf(bruno, alice, alexFemale, alex))

        assertEquals(
            listOf("Alex" to "F", "Alex" to "M", "Alice" to "F", "Bruno" to "M"),
            dataSource.observeNames(null, null, null).first().map { it.name to it.gender }
        )
    }

    @Test
    fun replaceAll_ignores_duplicate_name_gender_pairs_and_assigns_ids_from_one() = runTest {
        val dataSource = LocalStorageNameLocalDataSource()

        dataSource.replaceAll(listOf(alice, alice.copy(), bruno))

        val stored = dataSource.observeNames(null, null, null).first()
        assertEquals(listOf("Alice", "Bruno"), stored.map { it.name })
        assertEquals(listOf(1L, 2L), stored.map { it.id }.sorted())
    }

    @Test
    fun replaceAll_purges_what_was_there_before() = runTest {
        val dataSource = LocalStorageNameLocalDataSource()

        dataSource.replaceAll(listOf(alice, bruno))
        dataSource.replaceAll(listOf(alex))

        assertEquals(listOf("Alex"), dataSource.observeNames(null, null, null).first().map { it.name })
        assertEquals(1, dataSource.observeTotalCount().first())
    }

    @Test
    fun filters_by_gender_initial_and_case_insensitive_substring() = runTest {
        val dataSource = LocalStorageNameLocalDataSource()
        dataSource.replaceAll(listOf(alice, alex, bruno))

        assertEquals(
            listOf("Alice"),
            dataSource.observeNames(gender = "F", initial = null, query = null).first().map { it.name }
        )
        assertEquals(
            listOf("Alex", "Alice"),
            dataSource.observeNames(gender = null, initial = "A", query = null).first().map { it.name }
        )
        assertEquals(
            listOf("Alex", "Alice"),
            dataSource.observeNames(gender = null, initial = null, query = "aL").first().map { it.name }
        )
        assertEquals(1, dataSource.observeCount(gender = "M", initial = null, query = "run").first())
    }

    @Test
    fun observeNamesUsedByBothGenders_reports_only_names_stored_under_two_genders() = runTest {
        val dataSource = LocalStorageNameLocalDataSource()

        dataSource.replaceAll(listOf(alice, alex, alexFemale, bruno))

        assertEquals(listOf("Alex"), dataSource.observeNamesUsedByBothGenders().first())
    }

    @Test
    fun stored_names_survive_a_new_instance_reading_them_back() = runTest {
        LocalStorageNameLocalDataSource().replaceAll(listOf(alice, bruno))

        // Same browser, fresh object graph - i.e. what a page reload gets.
        val afterReload = LocalStorageNameLocalDataSource()

        assertEquals(
            listOf("Alice" to "A", "Bruno" to "B"),
            afterReload.observeNames(null, null, null).first().map { it.name to it.initialLetter }
        )
        assertEquals(2, afterReload.observeTotalCount().first())
    }
}
