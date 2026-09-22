import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        // Starts the same Koin graph the Android and web shells register - see composeApp's
        // MainViewController.kt.
        MainViewControllerKt.setupKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}
