import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // No version here: the Kotlin plugin is already on the build classpath via the other
    // modules' plugins, and re-declaring a version for it fails plugin resolution.
    kotlin("jvm")
    application
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("org.neteinstein.pickaname.tools.GenerateNamesSnapshotKt")
}

tasks.named<JavaExec>("run") {
    // Default output paths in the generator are repo-relative (webApp/src/...), so run from the
    // repo root rather than this module's directory.
    workingDir = rootProject.projectDir
}

dependencies {
    // The same parser the apps use, through its JVM target - the snapshot is only trustworthy
    // if it is produced by the code the app would have run itself.
    implementation(project(":core:model"))
    implementation(project(":core:parser"))
    implementation(libs.kotlinx.coroutines.core)
}
