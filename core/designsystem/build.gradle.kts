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
            // GenderTag (androidMain, see below) needs the Gender enum; core:model is a pure-
            // Kotlin leaf module so depending on it from commonMain costs nothing on any target.
            implementation(project(":core:model"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
        androidMain.dependencies {
            // GenderTag (see presentation/common/) is Android-only for now: it calls the classic
            // androidx.compose.ui.res.stringResource(@StringRes Int) overload against this
            // module's own androidMain strings.xml, and android.compose.material.icons-extended
            // isn't multiplatform - both stay android-only until Phase 3 moves feature:namelist
            // (GenderTag's only consumer) to Compose Multiplatform resources.
            implementation(libs.androidx.material.icons.extended)
        }
    }
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
