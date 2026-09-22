plugins {
    id("pickaname.kmp.library")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            // api, not implementation: :app's own DataStoreModule.kt builds the FlowSettings/
            // SharedPreferencesSettings directly, so it needs these on its own compile classpath
            // too (see MIGRATION_PLAN.md Phase 1's "leaky by design" note).
            api(libs.multiplatform.settings)
            api(libs.multiplatform.settings.coroutines)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            // Read-only, and only for the one-time migration off the app's pre-Phase-1 settings
            // file (LegacyDataStoreMigration) - nothing in the app writes DataStore any more.
            implementation(libs.androidx.datastore.preferences)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.truth)
                implementation(libs.turbine)
                // MapSettings lives here, not in the main multiplatform-settings artifact.
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}

