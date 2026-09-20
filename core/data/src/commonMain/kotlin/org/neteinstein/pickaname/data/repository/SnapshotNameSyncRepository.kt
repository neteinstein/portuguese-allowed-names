package org.neteinstein.pickaname.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.local.database.NameRecord
import org.neteinstein.pickaname.data.mapper.toFilterInitial
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.repository.NameSyncRepository

/**
 * Loads an already-parsed names snapshot instead of downloading and parsing the source PDF.
 *
 * This is how the **web** build syncs. It has to be: the official source serves no
 * `Access-Control-Allow-Origin` header, so a browser refuses the request outright, whatever HTTP
 * client makes it (MIGRATION_PLAN.md risk R3 - confirmed live, not assumed). A scheduled CI job
 * (`tools/names-snapshot`) parses the PDF with the same parser the apps use and publishes the
 * result next to the site, which turns the sync into a same-origin fetch of ~70 KB - and spares
 * every visitor's browser a 2.9 MB PDF parse.
 *
 * [syncFromUrl]'s `url` is deliberately ignored: the snapshot is whatever CI last published, and
 * the web build hides the source-URL setting for exactly that reason (see
 * `PlatformCapabilities.canConfigureNamesSource`).
 */
class SnapshotNameSyncRepository(
    private val httpClient: HttpClient,
    private val nameLocalDataSource: NameLocalDataSource,
    private val snapshotUrl: String = DEFAULT_SNAPSHOT_URL
) : NameSyncRepository {

    override suspend fun syncFromUrl(url: String): SyncOutcome {
        val snapshot = try {
            val response = httpClient.get(snapshotUrl)
            if (!response.status.isSuccess()) {
                return SyncOutcome.Error(
                    SyncFailureReason.NETWORK,
                    "Unexpected response ${response.status.value} for $snapshotUrl"
                )
            }
            response.bodyAsText()
        } catch (e: Throwable) {
            // Throwable, not Exception: a failed browser fetch surfaces as a JsException, which
            // extends Throwable directly (see NameSyncRepositoryImpl for the same trap).
            return SyncOutcome.Error(SyncFailureReason.NETWORK, e.message)
        }

        return try {
            val records = parseSnapshot(snapshot)
            if (records.isEmpty()) {
                SyncOutcome.Error(SyncFailureReason.NO_NAMES_FOUND)
            } else {
                nameLocalDataSource.replaceAll(records)
                SyncOutcome.Success(namesLoaded = records.size)
            }
        } catch (e: Throwable) {
            SyncOutcome.Error(SyncFailureReason.INVALID_SOURCE, e.message)
        }
    }

    /**
     * One `name<TAB>gender` per line, `#` comment lines carrying provenance (generation time,
     * source URL, count). Same reasoning as the local store's format: names come out of a PDF
     * table and contain neither tabs nor newlines, so this needs no escaping and no
     * serialization dependency.
     */
    private fun parseSnapshot(snapshot: String): List<NameRecord> =
        snapshot.lineSequence()
            .filterNot { it.isBlank() || it.startsWith(COMMENT_PREFIX) }
            .mapNotNull { line ->
                val fields = line.split(FIELD_SEPARATOR)
                if (fields.size != FIELD_COUNT) return@mapNotNull null
                val name = fields[0].trim()
                val gender = fields[1].trim()
                if (name.isEmpty() || gender !in VALID_GENDER_CODES) return@mapNotNull null
                NameRecord(
                    name = name,
                    gender = gender,
                    initialLetter = name.toFilterInitial()
                )
            }
            .toList()

    private companion object {
        /** Relative, so it is fetched from whatever origin the app itself was served from. */
        const val DEFAULT_SNAPSHOT_URL = "names-snapshot.tsv"
        const val COMMENT_PREFIX = "#"
        const val FIELD_SEPARATOR = "\t"
        const val FIELD_COUNT = 2
        val VALID_GENDER_CODES = setOf("F", "M")
    }
}
