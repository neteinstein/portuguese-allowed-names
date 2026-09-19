pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // PREFER_PROJECT: the Kotlin Multiplatform Gradle plugin's wasmJs/Node.js toolchain setup
    // (NodeJsRootPlugin) needs to register its own ivy repository (https://nodejs.org/dist) to
    // download Node.js, and that org.nodejs:node artifact only exists there - not on
    // google()/mavenCentral() below. FAIL_ON_PROJECT_REPOS rejects the plugin's repo outright
    // ("was added by unknown code"); PREFER_SETTINGS silently drops it instead (so the Node.js
    // lookup then 404s against google()/mavenCentral(), which don't have it either). Since
    // nothing else in this build declares its own per-module repositories, PREFER_PROJECT is
    // safe here: it lets that one plugin-added repo through while everything else still
    // resolves from google()/mavenCentral() as normal.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
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
