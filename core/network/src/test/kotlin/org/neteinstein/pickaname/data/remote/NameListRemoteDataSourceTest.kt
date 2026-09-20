package org.neteinstein.pickaname.data.remote

import com.google.common.truth.Truth.assertThat
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NameListRemoteDataSourceTest {

    private fun dataSource(engine: MockEngine) = NameListRemoteDataSource(HttpClient(engine))

    @Test
    fun `returns the response body bytes on a successful download`() = runTest {
        val bytes = byteArrayOf(1, 2, 3, 4)
        val engine = MockEngine { respond(content = bytes, status = HttpStatusCode.OK) }

        val result = dataSource(engine).downloadPdf("https://example.com/list.pdf")

        assertThat(result).isEqualTo(bytes)
    }

    @Test
    fun `sends a browser-like User-Agent header`() = runTest {
        var recordedUserAgent: String? = null
        val engine = MockEngine { request ->
            recordedUserAgent = request.headers[HttpHeaders.UserAgent]
            respond(content = byteArrayOf(1), status = HttpStatusCode.OK)
        }

        dataSource(engine).downloadPdf("https://example.com/list.pdf")

        assertThat(recordedUserAgent).contains("Mozilla")
    }

    @Test
    fun `throws on a non-2xx response`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.BadGateway) }

        val exception = runCatching {
            dataSource(engine).downloadPdf("https://example.com/list.pdf")
        }.exceptionOrNull()

        assertThat(exception).isNotNull()
    }

    @Test
    fun `throws on an empty response body`() = runTest {
        val engine = MockEngine { respond(content = ByteArray(0), status = HttpStatusCode.OK) }

        val exception = runCatching {
            dataSource(engine).downloadPdf("https://example.com/list.pdf")
        }.exceptionOrNull()

        assertThat(exception).isNotNull()
    }
}
