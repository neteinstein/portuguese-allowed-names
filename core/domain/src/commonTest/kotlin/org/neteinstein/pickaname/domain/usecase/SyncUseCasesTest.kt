package org.neteinstein.pickaname.domain.usecase

import kotlinx.coroutines.test.runTest
import org.neteinstein.pickaname.domain.model.AutoRefreshResult
import org.neteinstein.pickaname.domain.model.RefreshPeriod
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.fake.FakeNameSyncRepository
import org.neteinstein.pickaname.fake.FakeSettingsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Syncing and the periodic refresh decision, in `commonTest` so they run on every platform. The
 * clock is injected, as it is in production, so "is a refresh due?" is tested rather than timed.
 */
class SyncUseCasesTest {

    private val url = "https://example.com/list.pdf"
    private val now = 1_700_000_000_000L

    @Test
    fun a_successful_sync_stamps_the_refresh_time() = runTest {
        val settings = FakeSettingsRepository(sourceUrl = url)
        val outcome = SyncNamesUseCase(
            nameSyncRepository = FakeNameSyncRepository(SyncOutcome.Success(namesLoaded = 10)),
            settingsRepository = settings,
            currentTimeMillis = { now }
        )(url)

        assertEquals(SyncOutcome.Success(namesLoaded = 10), outcome)
        assertEquals(now, settings.lastRefreshTimestamp)
    }

    @Test
    fun a_failed_sync_leaves_the_refresh_time_alone_so_the_next_open_retries() = runTest {
        val settings = FakeSettingsRepository(sourceUrl = url)
        val outcome = SyncNamesUseCase(
            nameSyncRepository = FakeNameSyncRepository(SyncOutcome.Error(SyncFailureReason.NETWORK)),
            settingsRepository = settings,
            currentTimeMillis = { now }
        )(url)

        assertEquals(SyncOutcome.Error(SyncFailureReason.NETWORK), outcome)
        assertNull(settings.lastRefreshTimestamp)
    }

    @Test
    fun a_refresh_that_is_not_due_yet_does_nothing() = runTest {
        val settings = FakeSettingsRepository(
            sourceUrl = url,
            refreshPeriod = RefreshPeriod.YEARLY,
            lastRefreshTimestamp = now
        )
        val syncRepository = FakeNameSyncRepository(SyncOutcome.Success(namesLoaded = 10))

        val result = refreshUseCase(settings, syncRepository)()

        assertEquals(AutoRefreshResult.NotDue, result)
        assertEquals(emptyList(), syncRepository.syncedUrls)
    }

    @Test
    fun a_refresh_is_due_when_the_list_has_never_been_loaded() = runTest {
        val settings = FakeSettingsRepository(sourceUrl = url, lastRefreshTimestamp = null)
        val syncRepository = FakeNameSyncRepository(SyncOutcome.Success(namesLoaded = 10))

        val result = refreshUseCase(settings, syncRepository)()

        assertEquals(AutoRefreshResult.Refreshed(namesLoaded = 10), result)
        assertEquals(listOf(url), syncRepository.syncedUrls)
    }

    @Test
    fun a_refresh_is_due_once_the_configured_period_has_elapsed() = runTest {
        val settings = FakeSettingsRepository(
            sourceUrl = url,
            refreshPeriod = RefreshPeriod.WEEKLY,
            lastRefreshTimestamp = now - RefreshPeriod.WEEKLY.durationMillis
        )
        val syncRepository = FakeNameSyncRepository(SyncOutcome.Success(namesLoaded = 7))

        assertEquals(AutoRefreshResult.Refreshed(namesLoaded = 7), refreshUseCase(settings, syncRepository)())
    }

    @Test
    fun a_failed_refresh_reports_why() = runTest {
        val settings = FakeSettingsRepository(sourceUrl = url, lastRefreshTimestamp = null)
        val syncRepository = FakeNameSyncRepository(SyncOutcome.Error(SyncFailureReason.NETWORK))

        assertEquals(
            AutoRefreshResult.Failed(SyncFailureReason.NETWORK),
            refreshUseCase(settings, syncRepository)()
        )
    }

    private fun refreshUseCase(
        settings: FakeSettingsRepository,
        syncRepository: FakeNameSyncRepository
    ) = RefreshNamesIfDueUseCase(
        settingsRepository = settings,
        syncNamesUseCase = SyncNamesUseCase(syncRepository, settings) { now },
        currentTimeMillis = { now }
    )
}
