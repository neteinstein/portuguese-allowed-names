import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    // api, not implementation: MainDispatcherRule extends JUnit's TestWatcher and takes a
    // kotlinx-coroutines-test TestDispatcher in its public constructor, so consumers need both
    // on their own compile classpath (same lesson as core:database/core:datastore's Room/
    // multiplatform-settings dependencies).
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
