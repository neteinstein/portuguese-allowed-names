package org.neteinstein.pickaname.domain.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.neteinstein.pickaname.domain.model.NamesSourceDefaults
import org.neteinstein.pickaname.fake.FakeSettingsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The settings-facing use cases, in `commonTest` so they run on Android, wasmJs and iOS. Real
 * repository fakes rather than mocks (MockK is JVM-only), which also makes the assertions about
 * persisted state rather than about calls.
 */
class SettingsUseCasesTest {

    private val currentUrl = "https://current.example.com/list.pdf"

    @Test
    fun get_source_url_reads_the_configured_value() = runTest {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)

        assertEquals(currentUrl, GetSourceUrlUseCase(repository)())
    }

    @Test
    fun observe_source_url_emits_the_configured_value_and_then_updates() = runTest {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)

        ObserveSourceUrlUseCase(repository)().test {
            assertEquals(currentUrl, awaitItem())

            repository.setSourceUrl("https://newer.example.com/list.pdf")
            assertEquals("https://newer.example.com/list.pdf", awaitItem())
        }
    }

    @Test
    fun reset_restores_the_built_in_default() = runTest {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)

        ResetSourceUrlUseCase(repository)()

        assertEquals(NamesSourceDefaults.DEFAULT_SOURCE_URL, repository.getSourceUrl())
    }

    @Test
    fun update_persists_a_valid_url_and_trims_it() = runTest {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)

        val result = UpdateSourceUrlUseCase(repository)("  https://example.com/list.pdf  ")

        assertTrue(result.isSuccess)
        assertEquals("https://example.com/list.pdf", repository.getSourceUrl())
    }

    @Test
    fun update_rejects_an_invalid_url_and_leaves_the_old_one() = runTest {
        val repository = FakeSettingsRepository(sourceUrl = currentUrl)

        val result = UpdateSourceUrlUseCase(repository)("not a url")

        assertTrue(result.isFailure)
        assertEquals(currentUrl, repository.getSourceUrl())
    }
}
