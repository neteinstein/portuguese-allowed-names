import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "org.neteinstein.pickaname.core.datastore"
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
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    // api, not implementation: :app's own DataStoreModule.kt builds the FlowSettings/
    // SharedPreferencesSettings directly, so it needs these on its own compile classpath too.
    api(libs.multiplatform.settings)
    api(libs.multiplatform.settings.coroutines)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    // MapSettings lives here, not in the main multiplatform-settings artifact.
    testImplementation(libs.multiplatform.settings.test)
}
