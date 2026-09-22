plugins {
    id("pickaname.kmp.compose")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.animation)
            // api, not implementation: NavTransitions' types are part of this module's public
            // surface (composeApp passes them straight into NavHost), and composeApp builds the
            // graph itself, so it needs Navigation on its own compile classpath either way.
            api(libs.jetbrains.navigation.compose)
        }
    }
}

