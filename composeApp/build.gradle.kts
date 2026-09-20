@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
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
    // A real framework binary, not just compiled klibs: an iOS app shell imports this, and
    // linking is what proves every actual in the graph is actually there (see MIGRATION_PLAN.md
    // - there is no Xcode project in this repo yet, so linking is the strongest iOS check CI can
    // run).
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // The one module allowed to see every feature (MIGRATION_PLAN.md §3.1): it assembles
            // the nav graph and the Koin graph, and every platform shell just calls App().
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:navigation"))
            implementation(project(":core:network"))
            implementation(project(":core:datastore"))
            implementation(project(":core:database"))
            implementation(project(":core:parser"))
            implementation(project(":core:data"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:sync"))
            implementation(project(":feature:namelist"))
            implementation(project(":feature:settings"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            // api for both: the platform shells (:app's Application today, webApp's main())
            // start Koin themselves with appModules(), so they need Koin's own API - and the BOM
            // that versions it - on their own classpath.
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            // Android's half of platformModule(): Room + SharedPreferences (both need a Context,
            // hence koin-android's androidContext()) and the CIO engine.
            implementation(libs.koin.android)
            implementation(libs.ktor.client.cio)
        }
        iosMain.dependencies {
            // Ktor's Apple engine (NSURLSession) - the iOS half of platformModule().
            implementation(libs.ktor.client.darwin)
        }
        val wasmJsMain by getting {
            dependencies {
                // Ktor's browser engine (fetch) - the web half of platformModule().
                implementation(libs.ktor.client.js)
                implementation(libs.multiplatform.settings.make.observable)
            }
        }
    }
}

android {
    namespace = "org.neteinstein.pickaname.app"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
