package org.neteinstein.pickaname.next

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.neteinstein.pickaname.app.App

/**
 * Thin Android launcher for the new KMP/Compose-Multiplatform module tree. Runs under a
 * distinct applicationId (`org.neteinstein.pickaname.next`) so it can be installed and tested
 * side by side with the shipping `:app` module during the migration, without overwriting it.
 * See MIGRATION_PLAN.md — this replaces `:app` at the final cutover (Phase 6), at which point
 * it takes back the real applicationId.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
