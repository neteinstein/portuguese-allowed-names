import SwiftUI
import ComposeApp

/// The whole app is the shared Compose UI; this shell exists only to host it, exactly as
/// `:androidApp`'s MainActivity and `:webApp`'s main() do.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}
