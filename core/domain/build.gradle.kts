plugins {
    id("pickaname.kmp.library")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        val androidUnitTest by getting {
            dependencies {
                // The use-case tests moved here from :app (they were testing code that left in
                // Phase 0). Still JUnit/MockK/Truth rather than kotlin.test, so they stay
                // Android-only until Phase 5's commonTest port.
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.truth)
                implementation(libs.turbine)
            }
        }
    }
}

