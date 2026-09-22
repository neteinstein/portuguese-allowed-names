package org.neteinstein.pickaname

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.res.painterResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.neteinstein.pickaname.app.App

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate()/setContentView so the system splash theme takes
        // effect immediately at cold start, before Compose has drawn its first frame.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // The launcher icon stays app-identity, owned by this shell, and is handed to the
            // shared App() rather than looked up inside it (see MIGRATION_PLAN.md §3.1) - the
            // web and iOS shells pass their own.
            App(logo = painterResource(R.drawable.ic_launcher_foreground))
        }
    }
}
