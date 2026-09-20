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
            // Only ktor-client-core: NameListRemoteDataSource takes an already-built HttpClient
            // via constructor injection, so this module never touches a concrete engine itself.
            // The engine (CIO on Android, ktor-client-js on wasmJs once a web DI module exists)
            // is chosen by whoever constructs that HttpClient - today :app's own AppModule.kt,
            // same "leaky by design" split as core:datastore's FlowSettings.
            implementation(libs.ktor.client.core)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.truth)
                implementation(libs.ktor.client.mock)
            }
        }
    }
}

android {
    namespace = "org.neteinstein.pickaname.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
