import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinWasmJsTargetDsl

/**
 * Everything every multiplatform module in this repo agrees on: which targets exist, the JVM
 * level, the Android SDK levels, and how the wasmJs browser tests are run.
 *
 * Before this existed, all of that was copy-pasted into ~15 `build.gradle.kts` files, so adding
 * a target (iOS, most recently) or bumping `compileSdk` meant fifteen identical edits and the
 * chance of missing one. Modules now declare only what actually differs: their namespace and
 * their dependencies.
 *
 * `namespace` is derived from the module's path (`:core:model` becomes
 * `org.neteinstein.pickaname.core.model`), which is the convention every module already followed
 * by hand. A module that needs something else just sets `android { namespace = ... }` itself -
 * its own build script runs after this plugin, so it wins.
 */
class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {

    @OptIn(ExperimentalWasmDsl::class)
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.library")

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
            wasmJs {
                browser {
                    testTask {
                        useKarma {
                            useChromeHeadless()
                            // A second engine, on CI only: "it renders" is exactly the claim
                            // that can differ between browsers, and Kotlin/Wasm needs WasmGC,
                            // which engines shipped at different times. A dev machine may not
                            // have Firefox, and that shouldn't fail a local test run.
                            if (System.getenv("CI") != null) {
                                useFirefoxHeadless()
                            }
                        }
                    }
                }
            }
            iosArm64()
            iosSimulatorArm64()
        }

        extensions.configure<LibraryExtension> {
            namespace = ROOT_PACKAGE + path.replace(":", ".").lowercase()
            compileSdk = COMPILE_SDK

            defaultConfig {
                minSdk = MIN_SDK
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }

    private companion object {
        // The one place these change. :androidApp sets its own, since an application module
        // also needs targetSdk and the version fields.
        const val COMPILE_SDK = 36
        const val MIN_SDK = 23
        const val ROOT_PACKAGE = "org.neteinstein.pickaname"
    }
}
