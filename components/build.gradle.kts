import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.roborazzi)
}

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

mavenPublishing {
    publishToMavenCentral()
    if (providers.gradleProperty("RELEASE_SIGNING_ENABLED").orElse("true").get().toBoolean()) {
        signAllPublications()
    }
    pom {
        name = "KMPComponents"
        description = "Reusable Compose Multiplatform components backed by kommon design tokens"
        inceptionYear = "2026"
        url = "https://github.com/ArjangConsulting/KMPComponents"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "maniramezan"
                name = "Mani Ramezan"
            }
        }
        scm {
            url = "https://github.com/ArjangConsulting/KMPComponents"
            connection = "scm:git:git://github.com/ArjangConsulting/KMPComponents.git"
            developerConnection = "scm:git:ssh://git@github.com/ArjangConsulting/KMPComponents.git"
        }
    }
}
