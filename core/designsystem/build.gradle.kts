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

    sourceSets {
        commonMain.dependencies {
            // GenderTag needs the Gender enum; core:model is a pure-Kotlin leaf module so
            // depending on it from commonMain costs nothing on any target.
            implementation(project(":core:model"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            // api, not implementation: every consumer calls stringResource(Res.string.x) against
            // the Res class generated here, so they need this runtime on their own classpath.
            api(compose.components.resources)
            api(libs.compose.material.icons.extended)
        }
    }
}

compose.resources {
    // The app's strings live here so every feature module can reach them (Android resources
    // don't flow "backward" from :app), which means the generated Res class has to be public.
    publicResClass = true
    packageOfResClass = "org.neteinstein.pickaname.core.designsystem.resources"
    generateResClass = always
}

android {
    namespace = "org.neteinstein.pickaname.core.designsystem"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
