import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.neteinstein.pickaname.next"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.neteinstein.pickaname.next"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-kmp"
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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    // PDFBoxResourceLoader.init() at startup, same as :app (exposed as api by core:parser).
    implementation(project(":core:parser"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
}
