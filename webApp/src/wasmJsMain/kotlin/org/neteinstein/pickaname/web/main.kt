@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, org.jetbrains.compose.resources.ExperimentalResourceApi::class)

package org.neteinstein.pickaname.web

import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.neteinstein.pickaname.app.App
import org.neteinstein.pickaname.di.appModules
import pickaname.webapp.generated.resources.Res
import pickaname.webapp.generated.resources.app_logo

fun main() {
    // Same Koin graph the Android shell registers (composeApp's appModules()), with the browser
    // half of platformModule(): localStorage-backed stores and Ktor's fetch engine.
    startKoin {
        modules(appModules())
    }
    ComposeViewport(document.body!!) {
        App(logo = painterResource(Res.drawable.app_logo))
    }
}
