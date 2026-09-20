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
    // Not for an app: this target exists so CI can parse the source PDF when it generates the
    // web names snapshot (see tools/names-snapshot and MIGRATION_PLAN.md's R3 decision).
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(libs.kotlinx.coroutines.core)
        }
        val wasmJsMain by getting {
            dependencies {
                // pdf.js, bundled by webpack into the wasmJs distribution - the web counterpart
                // of pdfbox-android below. Pinned exactly: pdf.js's API and its worker layout
                // have changed shape across majors, and this module's interop is written against
                // this one.
                implementation(npm("pdfjs-dist", "5.4.149"))
            }
        }
        jvmMain.dependencies {
            implementation(libs.pdfbox)
        }
        androidMain.dependencies {
            // api, not implementation: PickANameApplication calls
            // PDFBoxResourceLoader.init(context) directly at startup, so :app needs this on its
            // own compile classpath too. Android-only - pdfbox-android isn't multiplatform.
            api(libs.pdfbox.android)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.truth)
            }
        }
        val wasmJsTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

android {
    namespace = "org.neteinstein.pickaname.core.parser"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
