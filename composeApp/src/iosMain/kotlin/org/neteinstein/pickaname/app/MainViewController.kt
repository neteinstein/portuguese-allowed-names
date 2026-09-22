@file:OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)

package org.neteinstein.pickaname.app

import androidx.compose.ui.window.ComposeUIViewController
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.neteinstein.pickaname.core.designsystem.resources.Res
import org.neteinstein.pickaname.core.designsystem.resources.app_logo
import org.neteinstein.pickaname.di.appModules
import platform.UIKit.UIViewController

/**
 * The entry point an iOS app shell calls: `MainViewController()` from SwiftUI/UIKit, exactly as
 * `:app`'s `MainActivity` calls [App] and `webApp`'s `main()` does on the web.
 *
 * `iosApp/` is the SwiftUI shell that calls it. Koin is started here rather than in Swift so the
 * three shells stay symmetrical: each one hands the shared app its brand mark and nothing else.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App(logo = painterResource(Res.drawable.app_logo))
}

/**
 * Called once by the iOS shell before the first [MainViewController].
 *
 * Not named `initKoin`: Objective-C reserves the `init` prefix for initialisers, so Kotlin/Native
 * exports such a function as `doInitKoin()`, and Swift callers would see that instead.
 */
fun setupKoin() {
    startKoin { modules(appModules()) }
}
