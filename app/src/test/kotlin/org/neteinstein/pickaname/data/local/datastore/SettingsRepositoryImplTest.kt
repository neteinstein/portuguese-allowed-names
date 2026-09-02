package org.neteinstein.pickaname.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.model.NamesSourceDefaults
import org.neteinstein.pickaname.domain.model.SearchEngine

/**
 * In-memory [DataStore] test double. Real [updateData] semantics (read-modify-write against the
 * current value, exposed reactively via [data]) without any disk I/O or Android runtime, so
 * [SettingsRepositoryImpl] can be exercised as a fast, pure-JVM unit test.
 */
private class FakeDataStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state.asStateFlow()

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

class SettingsRepositoryImplTest {

    @Test
    fun `observeSourceUrl defaults to the built-in url when nothing is persisted yet`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())

        repository.observeSourceUrl().test {
            assertThat(awaitItem()).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)
        }
    }

    @Test
    fun `getSourceUrl returns the default when nothing is persisted yet`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())

        assertThat(repository.getSourceUrl()).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)
    }

    @Test
    fun `setSourceUrl persists the new value so subsequent reads see it`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())

        repository.setSourceUrl("https://new.example.com/list.pdf")

        assertThat(repository.getSourceUrl()).isEqualTo("https://new.example.com/list.pdf")
    }

    @Test
    fun `observeSourceUrl emits the new value once it is updated`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())

        repository.observeSourceUrl().test {
            assertThat(awaitItem()).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)

            repository.setSourceUrl("https://new.example.com/list.pdf")

            assertThat(awaitItem()).isEqualTo("https://new.example.com/list.pdf")
        }
    }

    @Test
    fun `resetSourceUrlToDefault overwrites a previously persisted value`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())
        repository.setSourceUrl("https://stored.example.com/list.pdf")

        repository.resetSourceUrlToDefault()

        assertThat(repository.getSourceUrl()).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)
    }

    @Test
    fun `observeSearchEngine defaults to duckduckgo when nothing is persisted yet`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())

        repository.observeSearchEngine().test {
            assertThat(awaitItem()).isEqualTo(SearchEngine.DUCKDUCKGO)
        }
    }

    @Test
    fun `getSearchEngine returns the default when nothing is persisted yet`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())

        assertThat(repository.getSearchEngine()).isEqualTo(SearchEngine.DEFAULT)
    }

    @Test
    fun `setSearchEngine persists the new value so subsequent reads see it`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())

        repository.setSearchEngine(SearchEngine.GOOGLE)

        assertThat(repository.getSearchEngine()).isEqualTo(SearchEngine.GOOGLE)
    }

    @Test
    fun `observeSearchEngine emits the new value once it is updated`() = runTest {
        val repository = SettingsRepositoryImpl(FakeDataStore())

        repository.observeSearchEngine().test {
            assertThat(awaitItem()).isEqualTo(SearchEngine.DUCKDUCKGO)

            repository.setSearchEngine(SearchEngine.GOOGLE)

            assertThat(awaitItem()).isEqualTo(SearchEngine.GOOGLE)
        }
    }
}
