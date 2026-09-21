package org.neteinstein.pickaname.presentation.settings

import androidx.compose.runtime.Composable
import org.neteinstein.pickaname.domain.model.AppLanguage

/**
 * In-app language switching, for platforms that have no OS screen to hand off to.
 *
 * Android and iOS return `null` here and offer [rememberAppLanguageSettingsLauncher] instead -
 * the system setting is more familiar and applies to the whole app at once. The web has no such
 * screen, so it switches in-app.
 */
interface AppLanguageSelector {

    /** The language currently in effect, whether chosen explicitly or inherited from the browser. */
    val current: AppLanguage

    /** Persists [language] and applies it. May restart/reload the UI to re-resolve strings. */
    fun select(language: AppLanguage)
}

@Composable
expect fun rememberAppLanguageSelector(): AppLanguageSelector?
