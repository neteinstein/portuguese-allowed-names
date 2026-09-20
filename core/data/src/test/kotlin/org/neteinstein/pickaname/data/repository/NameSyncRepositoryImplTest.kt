package org.neteinstein.pickaname.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.neteinstein.pickaname.data.local.database.NameDao
import org.neteinstein.pickaname.data.local.database.NameEntity
import org.neteinstein.pickaname.data.parser.NameListTextParser
import org.neteinstein.pickaname.data.parser.ParsedName
import org.neteinstein.pickaname.data.parser.PdfTextExtractor
import org.neteinstein.pickaname.data.remote.NameListRemoteDataSource
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.SyncFailureReason
import org.neteinstein.pickaname.domain.model.SyncOutcome
import java.io.IOException

class NameSyncRepositoryImplTest {

    private val remoteDataSource: NameListRemoteDataSource = mockk()
    private val pdfTextExtractor: PdfTextExtractor = mockk()
    private val textParser: NameListTextParser = mockk()
    private val nameDao: NameDao = mockk(relaxed = true)

    private val repository = NameSyncRepositoryImpl(
        remoteDataSource = remoteDataSource,
        pdfTextExtractor = pdfTextExtractor,
        textParser = textParser,
        nameDao = nameDao
    )

    private val url = "https://example.com/list.pdf"

    @Test
    fun `successful sync purges and repopulates the database`() = runTest {
        val pdfBytes = byteArrayOf(1, 2, 3)
        coEvery { remoteDataSource.downloadPdf(url) } returns pdfBytes
        coEvery { pdfTextExtractor.extractText(pdfBytes) } returns "raw text"
        coEvery { textParser.parse("raw text") } returns listOf(
            ParsedName("Ada", Gender.FEMALE),
            ParsedName("Abel", Gender.MALE)
        )

        val outcome = repository.syncFromUrl(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Success(namesLoaded = 2))
        coVerify(exactly = 1) { nameDao.replaceAll(any()) }
    }

    @Test
    fun `deduplicates parsed names before persisting`() = runTest {
        val pdfBytes = byteArrayOf(1)
        coEvery { remoteDataSource.downloadPdf(url) } returns pdfBytes
        coEvery { pdfTextExtractor.extractText(pdfBytes) } returns "raw text"
        coEvery { textParser.parse("raw text") } returns listOf(
            ParsedName("Ada", Gender.FEMALE),
            ParsedName("Ada", Gender.FEMALE)
        )
        val slot = mutableListOf<List<NameEntity>>()
        coEvery { nameDao.replaceAll(capture(slot)) } returns Unit

        val outcome = repository.syncFromUrl(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Success(namesLoaded = 1))
        assertThat(slot.single()).hasSize(1)
    }

    @Test
    fun `network failure while downloading maps to a NETWORK error`() = runTest {
        coEvery { remoteDataSource.downloadPdf(url) } throws IOException("boom")

        val outcome = repository.syncFromUrl(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Error(SyncFailureReason.NETWORK, "boom"))
        coVerify(exactly = 0) { nameDao.replaceAll(any()) }
    }

    @Test
    fun `invalid url while downloading maps to an INVALID_SOURCE error`() = runTest {
        coEvery { remoteDataSource.downloadPdf(url) } throws IllegalArgumentException("bad url")

        val outcome = repository.syncFromUrl(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Error(SyncFailureReason.INVALID_SOURCE, "bad url"))
    }

    @Test
    fun `a document with no parseable names maps to a NO_NAMES_FOUND error`() = runTest {
        val pdfBytes = byteArrayOf(1)
        coEvery { remoteDataSource.downloadPdf(url) } returns pdfBytes
        coEvery { pdfTextExtractor.extractText(pdfBytes) } returns "garbage"
        coEvery { textParser.parse("garbage") } returns emptyList()

        val outcome = repository.syncFromUrl(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Error(SyncFailureReason.NO_NAMES_FOUND))
        coVerify(exactly = 0) { nameDao.replaceAll(any()) }
    }

    @Test
    fun `an unexpected failure while extracting text is surfaced as INVALID_SOURCE, not a crash`() = runTest {
        val pdfBytes = byteArrayOf(1)
        coEvery { remoteDataSource.downloadPdf(url) } returns pdfBytes
        coEvery { pdfTextExtractor.extractText(pdfBytes) } throws RuntimeException("corrupt pdf")

        val outcome = repository.syncFromUrl(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Error(SyncFailureReason.INVALID_SOURCE, "corrupt pdf"))
    }

    @Test
    fun `a database failure while persisting is surfaced as INVALID_SOURCE, not a crash`() = runTest {
        val pdfBytes = byteArrayOf(1)
        coEvery { remoteDataSource.downloadPdf(url) } returns pdfBytes
        coEvery { pdfTextExtractor.extractText(pdfBytes) } returns "raw text"
        coEvery { textParser.parse("raw text") } returns listOf(ParsedName("Ada", Gender.FEMALE))
        coEvery { nameDao.replaceAll(any()) } throws RuntimeException("disk full")

        val outcome = repository.syncFromUrl(url)

        assertThat(outcome).isEqualTo(SyncOutcome.Error(SyncFailureReason.INVALID_SOURCE, "disk full"))
    }
}
