@file:OptIn(
    com.russhwolf.settings.ExperimentalSettingsApi::class,
    kotlinx.coroutines.ExperimentalCoroutinesApi::class
)

package org.neteinstein.pickaname.data.local.datastore

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.domain.model.NamesSourceDefaults
import org.neteinstein.pickaname.domain.model.SearchEngine

class SettingsRepositoryImplTest {

    private fun newRepository() =
        SettingsRepositoryImpl(MapSettings().toFlowSettings(UnconfinedTestDispatcher()))

    @Test
    fun `observeSourceUrl defaults to the built-in url when nothing is persisted yet`() = runTest {
        val repository = newRepository()

        repository.observeSourceUrl().test {
            assertThat(awaitItem()).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)
        }
    }

    @Test
    fun `getSourceUrl returns the default when nothing is persisted yet`() = runTest {
        val repository = newRepository()

        assertThat(repository.getSourceUrl()).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)
    }

    @Test
    fun `setSourceUrl persists the new value so subsequent reads see it`() = runTest {
        val repository = newRepository()

        repository.setSourceUrl("https://new.example.com/list.pdf")

        assertThat(repository.getSourceUrl()).isEqualTo("https://new.example.com/list.pdf")
    }

    @Test
    fun `observeSourceUrl emits the new value once it is updated`() = runTest {
        val repository = newRepository()

        repository.observeSourceUrl().test {
            assertThat(awaitItem()).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)

            repository.setSourceUrl("https://new.example.com/list.pdf")

            assertThat(awaitItem()).isEqualTo("https://new.example.com/list.pdf")
        }
    }

    @Test
    fun `resetSourceUrlToDefault overwrites a previously persisted value`() = runTest {
        val repository = newRepository()
        repository.setSourceUrl("https://stored.example.com/list.pdf")

        repository.resetSourceUrlToDefault()

        assertThat(repository.getSourceUrl()).isEqualTo(NamesSourceDefaults.DEFAULT_SOURCE_URL)
    }

    @Test
    fun `observeSearchEngine defaults to brave when nothing is persisted yet`() = runTest {
        val repository = newRepository()

        repository.observeSearchEngine().test {
            assertThat(awaitItem()).isEqualTo(SearchEngine.BRAVE)
        }
    }

    @Test
    fun `getSearchEngine returns the default when nothing is persisted yet`() = runTest {
        val repository = newRepository()

        assertThat(repository.getSearchEngine()).isEqualTo(SearchEngine.DEFAULT)
    }

    @Test
    fun `setSearchEngine persists the new value so subsequent reads see it`() = runTest {
        val repository = newRepository()

        repository.setSearchEngine(SearchEngine.GOOGLE)

        assertThat(repository.getSearchEngine()).isEqualTo(SearchEngine.GOOGLE)
    }

    @Test
    fun `observeSearchEngine emits the new value once it is updated`() = runTest {
        val repository = newRepository()

        repository.observeSearchEngine().test {
            assertThat(awaitItem()).isEqualTo(SearchEngine.BRAVE)

            repository.setSearchEngine(SearchEngine.GOOGLE)

            assertThat(awaitItem()).isEqualTo(SearchEngine.GOOGLE)
        }
    }
}
