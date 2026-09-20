package org.neteinstein.pickaname.data.repository

import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.mapper.toRecord
import org.neteinstein.pickaname.data.parser.NameListTextParser
import org.neteinstein.pickaname.data.parser.PdfTextExtractor
import org.neteinstein.pickaname.data.remote.NameListRemoteDataSource
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import org.neteinstein.pickaname.domain.repository.NameSyncRepository

class NameSyncRepositoryImpl(
    private val remoteDataSource: NameListRemoteDataSource,
    private val pdfTextExtractor: PdfTextExtractor,
    private val textParser: NameListTextParser,
    private val nameLocalDataSource: NameLocalDataSource
) : NameSyncRepository {

    override suspend fun syncFromUrl(url: String): SyncOutcome {
        val pdfBytes = try {
            remoteDataSource.downloadPdf(url)
        } catch (e: IllegalArgumentException) {
            return SyncOutcome.Error(SyncFailureReason.INVALID_SOURCE, e.message)
        } catch (e: Throwable) {
            // Anything else from the download step - a bad status code, a timeout, a genuine
            // connectivity failure - is treated as a network problem. Kept as a broad catch
            // (rather than a specific IOException type) so this stays portable across the Ktor
            // engines each platform uses, instead of depending on one platform's exception
            // hierarchy.
            //
            // Throwable, not Exception, and that difference is load-bearing on web: a failed
            // browser fetch (a CORS rejection, say - see MIGRATION_PLAN.md risk R3) reaches
            // Kotlin/Wasm as a `JsException`, which extends Throwable directly. Catching only
            // Exception let it escape the coroutine, so the Sync screen sat on its spinner
            // forever instead of showing the error state.
            return SyncOutcome.Error(SyncFailureReason.NETWORK, e.message)
        }

        return try {
            val rawText = pdfTextExtractor.extractText(pdfBytes)
            val parsedNames = textParser.parse(rawText).distinct()
            if (parsedNames.isEmpty()) {
                SyncOutcome.Error(SyncFailureReason.NO_NAMES_FOUND)
            } else {
                val records = parsedNames.map { it.toRecord() }
                nameLocalDataSource.replaceAll(records)
                SyncOutcome.Success(namesLoaded = records.size)
            }
        } catch (e: Throwable) {
            // Anything from a corrupt PDF, an unexpected document layout, or a DB failure is
            // surfaced as a recoverable sync error rather than crashing the app. Throwable for
            // the same reason as the download step above.
            SyncOutcome.Error(SyncFailureReason.INVALID_SOURCE, e.message)
        }
    }
}
