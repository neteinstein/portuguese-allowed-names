import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Adds Compose Multiplatform on top of [KotlinMultiplatformLibraryConventionPlugin], for the
 * modules that actually draw something. Kept separate so the data-layer modules don't carry the
 * Compose plugins they never use.
 */
class KotlinMultiplatformComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        pluginManager.apply("pickaname.kmp.library")
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    }
}
