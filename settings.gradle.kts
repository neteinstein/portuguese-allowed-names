pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_SETTINGS rather than FAIL_ON_PROJECT_REPOS: the Kotlin Multiplatform Gradle
    // plugin's wasmJs/Node.js toolchain setup (NodeJsRootPlugin) needs to register its own
    // repository (https://nodejs.org/dist) to download Node.js - FAIL_ON_PROJECT_REPOS rejects
    // that outright ("was added by unknown code"), which is incompatible with using a wasmJs
    // target at all. PREFER_SETTINGS still gives google()/mavenCentral() below priority for
    // anything they can resolve, it just stops hard-failing on a plugin-added repo.
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pick-A-Name"
include(":app")
include(":core:model")
include(":core:domain")
include(":core:designsystem")
include(":composeApp")
include(":androidApp")
include(":webApp")
