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
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // The fakes implement the domain's repository interfaces.
            api(project(":core:model"))
            api(project(":core:domain"))
            // api, not implementation: every consumer's tests call runViewModelTest and then use
            // runTest's own API (advanceUntilIdle, runCurrent) on the scope it hands them.
            api(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            // MainDispatcherRule extends JUnit's TestWatcher and takes a TestDispatcher in its
            // public constructor, so the Android-only suites that still use it need both.
            api(libs.junit)
        }
    }
}

android {
    namespace = "org.neteinstein.pickaname.core.testing"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
