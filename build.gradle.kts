// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
}

/**
 * Fails the build if the module boundaries in MIGRATION_PLAN.md §3.1 are broken.
 *
 * Two rules, both of which are easy to violate by accident and invisible until the graph is a
 * knot:
 * - a `feature:*` module must not depend on another `feature:*` module (they talk through
 *   `core:*` and are assembled by `composeApp`, which is the one module allowed to see them all);
 * - a `core:*` module must not depend on a `feature:*` module - dependencies point inward.
 *
 * Runs as part of `check`, so it is covered by every CI job that builds the project.
 */
val checkModuleBoundaries by tasks.registering {
    group = "verification"
    description = "Verifies feature/core module dependency rules (MIGRATION_PLAN.md §3.1)."

    // Resolved at configuration time: the dependency declarations, not the resolved artifacts.
    val violations = subprojects.flatMap { project ->
        val path = project.path
        val isFeature = path.startsWith(":feature:")
        val isCore = path.startsWith(":core:")
        if (!isFeature && !isCore) return@flatMap emptyList()

        project.configurations.flatMap { configuration ->
            configuration.dependencies
                .filterIsInstance<ProjectDependency>()
                .map { it.path }
                .filter { dependencyPath ->
                    when {
                        isFeature -> dependencyPath.startsWith(":feature:") && dependencyPath != path
                        else -> dependencyPath.startsWith(":feature:")
                    }
                }
                .map { dependencyPath -> "$path depends on $dependencyPath" }
        }
    }.distinct()

    doLast {
        if (violations.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Module boundary violations (see MIGRATION_PLAN.md §3.1):")
                    violations.forEach { appendLine("  - $it") }
                    appendLine()
                    appendLine("Features talk through core modules; composeApp assembles them.")
                }
            )
        }
    }
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(checkModuleBoundaries)
    }
}
