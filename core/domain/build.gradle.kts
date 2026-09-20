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
            api(project(":core:model"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        val androidUnitTest by getting {
            dependencies {
                // The use-case tests moved here from :app (they were testing code that left in
                // Phase 0). Still JUnit/MockK/Truth rather than kotlin.test, so they stay
                // Android-only until Phase 5's commonTest port.
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.truth)
                implementation(libs.turbine)
            }
        }
    }
}

android {
    namespace = "org.neteinstein.pickaname.core.domain"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
