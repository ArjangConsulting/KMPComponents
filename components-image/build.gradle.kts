import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

// Async image loading, published separately so `components` does not pull in Coil and Ktor.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
}

// `./gradlew lint` (from the com.android.lint plugin) checks the Android and JVM sources and fails on
// errors. Dependabot owns version freshness, and AGP stays on the 9.1 series on purpose (see AGENTS.md).
val versionLintChecks = setOf("AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable")

kotlin {
    explicitApi()

    // `checkKotlinAbi` fails when the public surface changes without `updateKotlinAbi`.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()

    android {
        namespace = "io.github.maniramezan.kmpcomponents.image"
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

    // Same setup as :components: the common tests run in headless Chrome, and the executable binary
    // only exists so webpack bundles Skiko for them (CMP-4906). Consumers get an ordinary klib.
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    // `skikoTest` holds Compose UI tests shared by every Skiko-rendered target (desktop, iOS, web).
    // Android host tests have no Skiko renderer, so they only run `commonTest`.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("skiko") {
                withJvm()
                withIosArm64()
                withIosSimulatorArm64()
                withWasmJs()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // The public signature uses Compose UI types (Modifier, Painter, ContentScale).
            api(libs.compose.runtime)
            api(libs.compose.ui)
            implementation(libs.compose.foundation)
            // Coil stays an implementation detail: the public API exposes no Coil types.
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        // Coil's Ktor network fetcher needs one engine per platform.
        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.java)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("skikoTest").dependencies {
            implementation(libs.compose.ui.test)
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

lint { disable += versionLintChecks }

// `koverVerify` fails when line coverage drops below this floor (98.3% when it was set). Raise it as
// coverage grows; lowering it needs a reason in the PR.
kover {
    reports {
        verify {
            rule { minBound(95) }
        }
    }
}
