package org.neteinstein.pickaname.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * iOS has a per-app language screen too, inside the app's own Settings page - so this works the
 * same way the Android actual does, just through `UIApplicationOpenSettingsURLString`.
 */
@Composable
actual fun rememberAppLanguageSettingsLauncher(): (() -> Unit)? = remember {
    {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
    }
}
