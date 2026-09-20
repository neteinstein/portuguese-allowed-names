package org.neteinstein.pickaname.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.isSuccess

/**
 * Downloads the raw bytes of the names-list PDF.
 *
 * The IRN server returns a 502 for requests without a browser-like `User-Agent` header (observed
 * in practice), so we always send one.
 */
class NameListRemoteDataSource(
    private val httpClient: HttpClient
) {
    suspend fun downloadPdf(url: String): ByteArray {
        val response = httpClient.get(url) {
            header("User-Agent", BROWSER_USER_AGENT)
        }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Unexpected response ${response.status.value} while downloading $url")
        }
        val bytes = response.body<ByteArray>()
        if (bytes.isEmpty()) {
            throw IllegalStateException("Empty response body from $url")
        }
        return bytes
    }

    private companion object {
        const val BROWSER_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }
}
