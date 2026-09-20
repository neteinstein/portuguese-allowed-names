package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.repository.SettingsRepository

/**
 * Validates and persists a new names-source URL. Does not trigger a re-sync itself — callers
 * (Settings screen) are expected to follow a successful update with [SyncNamesUseCase].
 */
class UpdateSourceUrlUseCase(private val settingsRepository: SettingsRepository) {

    suspend operator fun invoke(url: String): Result<Unit> {
        val trimmed = url.trim()
        if (!isValidHttpUrl(trimmed)) {
            return Result.failure(IllegalArgumentException("Invalid URL"))
        }
        settingsRepository.setSourceUrl(trimmed)
        return Result.success(Unit)
    }
}

/**
 * Multiplatform stand-in for the `java.net.URI`-based check this used to do: accepts only
 * `http`/`https` URLs that actually carry a host. Hand-rolled rather than pulled from a library
 * because `java.net.URI` has no wasmJs equivalent and this is the only URL parsing the domain
 * layer does - it never needs the parsed parts, only a yes/no answer.
 */
internal fun isValidHttpUrl(value: String): Boolean {
    if (value.isBlank()) return false
    // java.net.URI rejects unencoded whitespace outright ("not a url at all"); keep that.
    if (value.any { it.isWhitespace() }) return false

    val schemeEnd = value.indexOf("://")
    if (schemeEnd <= 0) return false
    val scheme = value.substring(0, schemeEnd).lowercase()
    if (scheme != "http" && scheme != "https") return false

    val afterScheme = value.substring(schemeEnd + 3)
    val authority = afterScheme.takeWhile { it != '/' && it != '?' && it != '#' }
    // Drop userinfo ("user:pass@host") and the port, leaving the bare host.
    val host = authority.substringAfterLast('@').substringBefore(':')
    return host.isNotBlank()
}
