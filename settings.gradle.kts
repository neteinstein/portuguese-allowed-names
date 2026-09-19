pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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
