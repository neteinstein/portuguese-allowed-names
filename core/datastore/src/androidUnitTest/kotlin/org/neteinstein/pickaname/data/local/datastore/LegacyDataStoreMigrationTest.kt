package org.neteinstein.pickaname.data.local.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.common.truth.Truth.assertThat
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Covers the one-time copy out of the pre-Phase-1 Preferences DataStore file. Writes a real
 * legacy file with DataStore itself rather than a fixture, so the test would notice if the
 * on-disk format the app used ever stopped being readable this way.
 */
class LegacyDataStoreMigrationTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val settings = MapSettings()

    @Test
    fun `copies every legacy setting into the new store`() = runTest {
        val legacyFile = writeLegacyPreferences(
            sourceUrl = "https://example.com/custom.pdf",
            refreshPeriod = "WEEKLY",
            searchEngine = "BRAVE",
            lastRefresh = 1_700_000_000_000L
        )

        migrateLegacyDataStoreSettings(legacyFile, settings)

        assertThat(settings.getStringOrNull(SettingsKeys.SOURCE_URL))
            .isEqualTo("https://example.com/custom.pdf")
        assertThat(settings.getStringOrNull(SettingsKeys.REFRESH_PERIOD)).isEqualTo("WEEKLY")
        assertThat(settings.getStringOrNull(SettingsKeys.SEARCH_ENGINE)).isEqualTo("BRAVE")
        assertThat(settings.getLongOrNull(SettingsKeys.LAST_REFRESH_TIMESTAMP))
            .isEqualTo(1_700_000_000_000L)
    }

    @Test
    fun `deletes the legacy file so it only ever migrates once`() = runTest {
        val legacyFile = writeLegacyPreferences(sourceUrl = "https://example.com/custom.pdf")

        migrateLegacyDataStoreSettings(legacyFile, settings)

        assertThat(legacyFile.exists()).isFalse()
    }

    @Test
    fun `never overwrites a value the new store already has`() = runTest {
        settings.putString(SettingsKeys.SOURCE_URL, "https://example.com/newer.pdf")
        val legacyFile = writeLegacyPreferences(sourceUrl = "https://example.com/older.pdf")

        migrateLegacyDataStoreSettings(legacyFile, settings)

        assertThat(settings.getStringOrNull(SettingsKeys.SOURCE_URL))
            .isEqualTo("https://example.com/newer.pdf")
    }

    @Test
    fun `leaves unset legacy values alone instead of writing defaults`() = runTest {
        val legacyFile = writeLegacyPreferences(sourceUrl = "https://example.com/custom.pdf")

        migrateLegacyDataStoreSettings(legacyFile, settings)

        assertThat(settings.getStringOrNull(SettingsKeys.REFRESH_PERIOD)).isNull()
        assertThat(settings.getStringOrNull(SettingsKeys.SEARCH_ENGINE)).isNull()
        assertThat(settings.getLongOrNull(SettingsKeys.LAST_REFRESH_TIMESTAMP)).isNull()
    }

    @Test
    fun `does nothing when there is no legacy file`() = runTest {
        val absentFile = File(temporaryFolder.root, "never-written.preferences_pb")

        migrateLegacyDataStoreSettings(absentFile, settings)

        assertThat(settings.keys).isEmpty()
    }

    @Test
    fun `leaves an unreadable legacy file in place instead of failing the launch`() = runTest {
        val corruptFile = File(temporaryFolder.root, "corrupt.preferences_pb").apply {
            writeText("this is not a preferences protobuf")
        }

        migrateLegacyDataStoreSettings(corruptFile, settings)

        // The point is that a launch survives it and nothing bogus lands in the new store; what
        // DataStore does with the file it couldn't parse is DataStore's business, not this
        // migration's, so it isn't asserted here.
        assertThat(settings.keys).isEmpty()
    }

    /**
     * Writes a real DataStore file and hands back a **copy** of it.
     *
     * DataStore refuses to have two instances open on one file for the life of a process, and
     * the registration it keeps is only released asynchronously when the writer's scope is
     * cancelled - so handing the migration the very file this wrote is a race. It passed
     * locally every time and failed on CI, which is the tell. Copying the bytes sidesteps the
     * registry entirely while still testing against a file DataStore really wrote.
     */
    private suspend fun writeLegacyPreferences(
        sourceUrl: String? = null,
        refreshPeriod: String? = null,
        searchEngine: String? = null,
        lastRefresh: Long? = null
    ): File {
        val unique = System.nanoTime()
        val written = File(temporaryFolder.root, "written-$unique.preferences_pb")
        PreferenceDataStoreFactory.create(produceFile = { written }).edit { preferences ->
            sourceUrl?.let { preferences[stringPreferencesKey(SettingsKeys.SOURCE_URL)] = it }
            refreshPeriod?.let { preferences[stringPreferencesKey(SettingsKeys.REFRESH_PERIOD)] = it }
            searchEngine?.let { preferences[stringPreferencesKey(SettingsKeys.SEARCH_ENGINE)] = it }
            lastRefresh?.let { preferences[longPreferencesKey(SettingsKeys.LAST_REFRESH_TIMESTAMP)] = it }
        }

        val legacyFile = File(temporaryFolder.root, "legacy-$unique.preferences_pb")
        written.copyTo(legacyFile)
        return legacyFile
    }
}
