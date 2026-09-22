plugins {
    id("pickaname.kmp.compose")
}

kotlin {

    sourceSets {
        commonMain.dependencies {
            // GenderTag needs the Gender enum; core:model is a pure-Kotlin leaf module so
            // depending on it from commonMain costs nothing on any target.
            implementation(project(":core:model"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            // api, not implementation: every consumer calls stringResource(Res.string.x) against
            // the Res class generated here, so they need this runtime on their own classpath.
            api(compose.components.resources)
            api(libs.compose.material.icons.extended)
        }
    }
}

compose.resources {
    // The app's strings live here so every feature module can reach them (Android resources
    // don't flow "backward" from :app), which means the generated Res class has to be public.
    publicResClass = true
    packageOfResClass = "org.neteinstein.pickaname.core.designsystem.resources"
    generateResClass = always
}

