package org.neteinstein.pickaname.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberAppLanguageSettingsLauncher(): (() -> Unit)? {
    val context = LocalContext.current
    return remember(context) { { openAppLocaleSettings(context) } }
}

private fun openAppLocaleSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    }.apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}
