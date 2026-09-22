plugins {
    id("pickaname.kmp.library")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            // Only ktor-client-core: NameListRemoteDataSource takes an already-built HttpClient
            // via constructor injection, so this module never touches a concrete engine itself.
            // The engine (CIO on Android, ktor-client-js on wasmJs once a web DI module exists)
            // is chosen by whoever constructs that HttpClient - today :app's own AppModule.kt,
            // same "leaky by design" split as core:datastore's FlowSettings.
            implementation(libs.ktor.client.core)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.truth)
                implementation(libs.ktor.client.mock)
            }
        }
    }
}

