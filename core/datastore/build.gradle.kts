@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    wasmJs {
        browser()
    }

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

android {
    namespace = "org.neteinstein.pickaname.core.datastore"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
