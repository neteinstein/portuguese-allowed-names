plugins {
    id("pickaname.kmp.compose")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            // Strings (Compose Multiplatform resources) and shared theming/composables.
            implementation(project(":core:designsystem"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.compose.material.icons.extended)

            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.jetbrains.lifecycle.runtime.compose)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            // Custom Tabs + the WebView-backed in-app browser (see InAppBrowser.android.kt).
            implementation(libs.androidx.browser)
            implementation(libs.androidx.core.ktx)
            // BackHandler (see PlatformBackHandler.android.kt).
            implementation(libs.androidx.activity.compose)
        }
        val wasmJsMain by getting {
            dependencies {
                // window.open for the external-link actual.
                implementation(libs.kotlinx.browser)
            }
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(project(":core:testing"))
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.truth)
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

