@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.neteinstein.pickaname.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.neteinstein.pickaname.domain.model.AppLanguage
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Web language switching.
 *
 * Compose Multiplatform resolves resource strings from the browser's own language and offers no
 * public API to override that at runtime (`ComposeEnvironment` is internal, in 1.10 and 1.11
 * alike). So the choice is applied the way the browser allows: stored in `localStorage`, put in
 * front of `navigator.language(s)` at startup - before Compose reads them, see
 * `applyStoredAppLanguage()` - and picked up by reloading the page.
 *
 * A reload on an explicit language change is normal for a web app, and it keeps every string
 * resolved by one mechanism rather than two.
 */
@Composable
actual fun rememberAppLanguageSelector(): AppLanguageSelector? = remember { WebAppLanguageSelector }

private object WebAppLanguageSelector : AppLanguageSelector {

    override val current: AppLanguage
        get() = storedAppLanguage() ?: AppLanguage.fromTagOrDefault(browserLanguageTag())

    override fun select(language: AppLanguage) {
        if (language == current) return
        runCatching { localStorage[APP_LANGUAGE_KEY] = language.code }
        // Strings are resolved once, from the browser's language, so the new choice only takes
        // effect on the next load - applyStoredAppLanguage() runs before Compose starts.
        window.location.reload()
    }
}

private fun storedAppLanguage(): AppLanguage? =
    localStorage[APP_LANGUAGE_KEY]?.let { code -> AppLanguage.entries.firstOrNull { it.code == code } }

private fun browserLanguageTag(): String? = window.navigator.language.takeIf { it.isNotBlank() }

/**
 * Makes a stored language choice the one the browser reports, so Compose's resource lookup uses
 * it. Must run before the first composition; with nothing stored it does nothing at all, leaving
 * the genuine browser language (and therefore English for anything the app has no strings for).
 */
fun applyStoredAppLanguage() {
    val stored = storedAppLanguage() ?: return
    overrideNavigatorLanguage(stored.code)
}

private fun overrideNavigatorLanguage(languageCode: String) {
    js(
        """{
            Object.defineProperty(navigator, 'language', { get: () => languageCode, configurable: true });
            Object.defineProperty(navigator, 'languages', { get: () => [languageCode], configurable: true });
        }"""
    )
}

private const val APP_LANGUAGE_KEY = "pick_a_name.app_language"
