plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "org.neteinstein.pickaname"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.neteinstein.pickaname"
        minSdk = 23
        targetSdk = 36
        // CI (see .github/workflows/release.yml) passes the real value via
        // -PversionCode=<GitHub Actions run number>, so every released build gets a
        // versionCode that's always higher than the last - required for Android to treat it
        // as an upgrade. Local/dev builds that don't pass that property fall back to 1.
        versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = "2.2.28"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            // Intentionally left blank: values are supplied at build time via
            // -Pandroid.injected.signing.* Gradle properties (see
            // .github/workflows/release.yml), so no keystore secrets ever need to be
            // committed. Local/dev builds that don't pass those properties fall back to
            // an unsigned release build (see the `hasProperty` check below).
        }
    }

    buildTypes {
        release {
            if (project.hasProperty("android.injected.signing.store.file")) {
                signingConfig = signingConfigs.getByName("release")
            }
            // R8 in full mode (the AGP 8 default): shrinks, optimizes and obfuscates the code,
            // which is what collapses the release build from 5 dex files down to 1 - fewer
            // classes to verify and load means a faster cold start, on top of the smaller
            // download. `isShrinkResources` then drops the resources that the shrunk code no
            // longer references. Everything that is looked up by name at runtime is pinned in
            // proguard-rules.pro; see that file for what and why.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    buildFeatures {
        compose = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/*.RSA"
            excludes += "/META-INF/*.SF"
            excludes += "/META-INF/*.DSA"
        }
    }
}

dependencies {
    // core modules (see MIGRATION_PLAN.md) - domain layer, design system, and the whole data
    // layer now live here; package names are unchanged from before each move, so no import in
    // this module's own source needed to change.
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:database"))
    implementation(project(":core:parser"))
    implementation(project(":core:data"))
    implementation(project(":feature:settings"))

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.browser)

    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Dependency Injection - Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Networking (names source download) - the shared HttpClient built and provided from here
    // (see di/AppModule.kt) since it's the only place that knows which Koin modules exist;
    // Room/DataStore/OkHttp/pdfbox-android direct dependencies moved out with the code that used
    // them (see the core/* modules above).
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing dependencies
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.koin.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
