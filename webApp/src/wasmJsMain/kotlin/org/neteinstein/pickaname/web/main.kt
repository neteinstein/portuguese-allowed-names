@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, org.jetbrains.compose.resources.ExperimentalResourceApi::class)

package org.neteinstein.pickaname.web

import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.neteinstein.pickaname.app.App
import org.neteinstein.pickaname.di.appModules
import org.neteinstein.pickaname.presentation.settings.applyStoredAppLanguage
import pickaname.webapp.generated.resources.Res
import pickaname.webapp.generated.resources.app_logo

fun main() {
    // Before anything composes: if the visitor picked a language in Settings, make that the
    // language the browser reports, since that is what Compose resolves strings from. With no
    // choice stored this does nothing, so the page follows the browser (and falls back to
    // English for languages the app has no strings for).
    applyStoredAppLanguage()

    // Same Koin graph the Android shell registers (composeApp's appModules()), with the browser
    // half of platformModule(): localStorage-backed stores and Ktor's fetch engine.
    startKoin {
        modules(appModules())
    }
    ComposeViewport(document.body!!) {
        App(logo = painterResource(Res.drawable.app_logo))
    }
}
