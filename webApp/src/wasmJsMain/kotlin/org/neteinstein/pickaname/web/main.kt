@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package org.neteinstein.pickaname.web

import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.neteinstein.pickaname.app.App

fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}
