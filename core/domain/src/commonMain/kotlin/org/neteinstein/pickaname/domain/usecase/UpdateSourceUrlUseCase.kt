package org.neteinstein.pickaname.domain.usecase

import org.neteinstein.pickaname.domain.repository.SettingsRepository
import java.net.URI

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

    private fun isValidHttpUrl(value: String): Boolean {
        if (value.isBlank()) return false
        val uri = runCatching { URI(value) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase()
        return (scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()
    }
}
