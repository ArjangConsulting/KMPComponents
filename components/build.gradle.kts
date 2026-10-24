import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
}

// `./gradlew lint` (from the com.android.lint plugin) checks the Android and JVM sources and fails on
// errors. Dependabot owns version freshness, and AGP stays on the 9.1 series on purpose (see AGENTS.md).
val versionLintChecks = setOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable")

kotlin {
    explicitApi()

    // Guards the published API: `checkKotlinAbi` (part of `check`) fails when the public surface
    // changes without `./gradlew :components:updateKotlinAbi` updating components/api/.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()

    android {
        namespace = "io.github.maniramezan.kmpcomponents"
        compileSdk = 37
        minSdk = 26
        compilerOptions.jvmTarget = JvmTarget.JVM_17
        withHostTest {}
        lint { disable += versionLintChecks }
    }
    jvm {
        compilerOptions.jvmTarget = JvmTarget.JVM_17
    }
    iosArm64()
    iosSimulatorArm64()

    // Browser rather than Node: Compose for Web renders through Skiko's Wasm runtime, which the
    // common tests load too. Running them needs a local Chrome, which the macOS CI runner provides.
    // The executable binary only exists so webpack bundles that runtime for the tests (CMP-4906);
    // consumers still get an ordinary klib.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kommon.design.system)
            api(libs.compose.runtime)
            api(libs.compose.material3)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            // Compose UI tests run on desktop only: they need a Skiko renderer, which Android host
            // tests lack.
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.ui.test)
            implementation(libs.roborazzi.compose.desktop)
        }
    }
}

lint { disable += versionLintChecks }

// `koverVerify` fails when line coverage drops below this floor (92.9% when it was set). Raise it as
// coverage grows; lowering it needs a reason in the PR.
kover {
    reports {
        verify {
            rule { minBound(90) }
        }
    }
}
