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
 * There is no Xcode project in this repo yet (see MIGRATION_PLAN.md §9.8 on iOS scope), so this
 * is currently validated by compiling and linking the framework rather than by running an app.
 * Koin is started here rather than in the shell so that stays true for whatever shell arrives.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App(logo = painterResource(Res.drawable.app_logo))
}

/** Called once by the iOS shell before the first [MainViewController]. */
fun initKoin() {
    startKoin { modules(appModules()) }
}
