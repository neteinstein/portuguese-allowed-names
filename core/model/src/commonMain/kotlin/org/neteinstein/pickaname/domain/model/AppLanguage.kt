package org.neteinstein.pickaname.domain.model

/**
 * The languages the app ships strings for.
 *
 * Only platforms without an OS-level per-app language screen need this (the web build): Android
 * and iOS hand the user off to the system setting instead, which is both more familiar and
 * applies everywhere at once.
 */
enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    PORTUGUESE("pt");

    companion object {
        /**
         * The best match for a platform language tag (`pt-PT`, `en-GB`, ...), or [ENGLISH] when
         * the app has no strings for it - which mirrors how the resource system itself falls
         * back to the default `values/` table.
         */
        fun fromTagOrDefault(languageTag: String?): AppLanguage =
            entries.firstOrNull { languageTag?.startsWith(it.code, ignoreCase = true) == true }
                ?: ENGLISH
    }
}
