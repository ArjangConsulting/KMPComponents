import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

// Async image loading, published separately so `components` does not pull in Coil and Ktor.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
}

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
    }
    jvm {
        compilerOptions.jvmTarget = JvmTarget.JVM_17
    }
    iosArm64()
    iosSimulatorArm64()

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
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.compose.ui.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    if (providers.gradleProperty("RELEASE_SIGNING_ENABLED").orElse("true").get().toBoolean()) {
        signAllPublications()
    }
    coordinates(artifactId = "components-image")
    pom {
        name = "KMPComponents Image"
        description = "Cached async image loading for KMPComponents, backed by Coil"
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
