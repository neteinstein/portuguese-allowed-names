@file:OptIn(
    // compose.uiTest, used by the browser smoke test below.
    org.jetbrains.compose.ExperimentalComposeLibrary::class,
)

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("pickaname.kmp.compose")
}

kotlin {
    // A real framework binary, not just compiled klibs: iosApp imports this, and linking is what
    // proves every actual in the graph is really there. Configured across whichever native
    // targets the convention plugin declared, rather than naming them again here.
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.framework {
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
        val wasmJsTest by getting {
            dependencies {
                // Composes the real App() in a real browser - see AppRuntimeSmokeTest.
                implementation(kotlin("test"))
                implementation(compose.uiTest)
            }
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
    // The one module whose namespace isn't its path: everything else is derived by the
    // convention plugin, and the rest of this block (SDK levels, Java level) comes from there.
    namespace = "org.neteinstein.pickaname.app"
}
