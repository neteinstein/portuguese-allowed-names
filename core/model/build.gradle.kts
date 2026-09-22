plugins {
    id("pickaname.kmp.library")
}

kotlin {
    // This module's extra target: the snapshot generator that runs in CI needs the domain models
    // on the JVM (see core:parser's jvm target and tools/names-snapshot).
    jvm()

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
