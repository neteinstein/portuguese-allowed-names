@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package org.neteinstein.pickaname.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.jetbrains.compose.resources.stringResource
import org.neteinstein.pickaname.core.designsystem.resources.Res
import org.neteinstein.pickaname.core.designsystem.resources.cd_dismiss_install_banner
import org.neteinstein.pickaname.core.designsystem.resources.web_install_banner_action
import org.neteinstein.pickaname.core.designsystem.resources.web_install_banner_message
import org.neteinstein.pickaname.core.designsystem.resources.web_install_banner_title
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Shown only to Android visitors who haven't dismissed it. The dismissal is remembered in
 * `localStorage`, so it stays dismissed across reloads and sessions on that browser.
 */
@Composable
actual fun InstallAppBanner() {
    var dismissed by remember { mutableStateOf(isBannerDismissed()) }
    if (dismissed || !isAndroidBrowser()) return

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.web_install_banner_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(Res.string.web_install_banner_message),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(onClick = { openPlayStoreListing() }) {
                Text(stringResource(Res.string.web_install_banner_action))
            }
            IconButton(
                onClick = {
                    rememberBannerDismissed()
                    dismissed = true
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(Res.string.cd_dismiss_install_banner)
                )
            }
        }
    }
}

/**
 * True for Android phones *and* tablets. Tablet browsers drop the "Mobile" token from their
 * user-agent but keep "Android", so matching on "Android" alone is what covers both - while
 * still excluding desktop, iOS and ChromeOS, where this app has nothing to install.
 */
private fun isAndroidBrowser(): Boolean =
    window.navigator.userAgent.contains("Android", ignoreCase = true)

private fun isBannerDismissed(): Boolean = localStorage[DISMISSED_KEY] == "true"

private fun rememberBannerDismissed() {
    // A full storage quota shouldn't break dismissing the banner; it just won't be remembered
    // past this page load.
    runCatching { localStorage[DISMISSED_KEY] = "true" }
}

private fun openPlayStoreListing() {
    window.open(PLAY_STORE_URL, "_blank", "noopener")
}

private const val DISMISSED_KEY = "pick_a_name.install_banner_dismissed"
private const val PLAY_STORE_URL =
    "https://play.google.com/store/apps/details?id=org.neteinstein.pickaname"
