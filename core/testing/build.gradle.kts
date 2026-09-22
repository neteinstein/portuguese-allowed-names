plugins {
    id("pickaname.kmp.library")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            // The fakes implement the domain's repository interfaces.
            api(project(":core:model"))
            api(project(":core:domain"))
            // api, not implementation: every consumer's tests call runViewModelTest and then use
            // runTest's own API (advanceUntilIdle, runCurrent) on the scope it hands them.
            api(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            // MainDispatcherRule extends JUnit's TestWatcher and takes a TestDispatcher in its
            // public constructor, so the Android-only suites that still use it need both.
            api(libs.junit)
        }
    }
}

