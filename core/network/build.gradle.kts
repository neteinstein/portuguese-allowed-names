import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.ktor.client.core)
    // CIO rather than the OkHttp engine: ktor-client-okhttp 3.6.0 transitively pulls in
    // okhttp-android 5.5.0, which requires compileSdk 37+ (this repo is on 36 - see
    // MIGRATION_PLAN.md). CIO is a pure-Kotlin/coroutines engine with no such AAR metadata
    // constraint, and is equally fine for our plain GET-and-download-bytes use case.
    implementation(libs.ktor.client.cio)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.ktor.client.mock)
}
