package org.neteinstein.pickaname.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.neteinstein.pickaname.presentation.navigation.PickANameNavHost
import org.neteinstein.pickaname.presentation.theme.PickANameTheme

/**
 * Root composable shared by every platform shell: theme + nav graph, nothing platform-specific.
 *
 * [logo] is the brand mark the splash screen shows - each shell passes its own (Android its
 * launcher drawable, web the icon it ships), keeping app identity out of the shared code the
 * same way [org.neteinstein.pickaname.presentation.splash.SplashScreen] does.
 *
 * Koin is expected to be started by the shell before this is composed (see
 * [org.neteinstein.pickaname.di.appModules]).
 */
@Composable
fun App(logo: Painter) {
    PickANameTheme {
        PickANameNavHost(logo = logo)
    }
}
