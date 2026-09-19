package org.neteinstein.pickaname.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.neteinstein.pickaname.presentation.theme.PickANameTheme

/**
 * Root composable shared by every platform shell (Android, web, and — later — iOS). A
 * placeholder for now, proving the Android + wasmJs build pipeline end to end; real features
 * move in here feature-module by feature-module per MIGRATION_PLAN.md.
 */
@Composable
fun App() {
    PickANameTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Pick-A-Name", style = MaterialTheme.typography.headlineLarge)
                Text(text = "KMP + Compose Multiplatform migration in progress.")
            }
        }
    }
}
