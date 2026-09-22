plugins {
    id("pickaname.kmp.library")
    // Module-specific, so it stays here rather than in the convention plugin: this is the only
    // module with an annotation processor (Room's, for the Android target).
    alias(libs.plugins.ksp)
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:model"))
            implementation(project(":core:domain"))
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            // api, not implementation: :app's own DatabaseModule.kt builds the Room database
            // directly (Room.databaseBuilder(...)), so it needs these on its own compile
            // classpath too.
            api(libs.room.runtime)
            api(libs.room.ktx)
        }
        val wasmJsMain by getting {
            dependencies {
                // localStorage + the org.w3c.dom.Storage type the web store is built on.
                implementation(libs.kotlinx.browser)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.mockk)
                implementation(libs.truth)
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val wasmJsTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

// Room's annotation processor only runs for the Android target - AppDatabase/NameDao/NameEntity
// are androidMain-only, since room-runtime publishes no wasm-js artifact at all (risk R1).
dependencies {
    add("kspAndroid", libs.room.compiler)
}

