plugins {
    id("pickaname.kmp.library")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(project(":core:network"))
            implementation(project(":core:parser"))
            implementation(project(":core:database"))
            // SnapshotNameSyncRepository fetches the published snapshot itself, so this module
            // needs Ktor's client API directly (core:network's data source is PDF-specific).
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.truth)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

