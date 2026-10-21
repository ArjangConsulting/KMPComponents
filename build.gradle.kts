plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.roborazzi) apply false
}

allprojects {
    group = "io.github.maniramezan.kmpcomponents"
    version = providers.gradleProperty("VERSION_NAME").orElse("0.0.0-SNAPSHOT").get()
}

val ktlintVersion = libs.versions.ktlint.get()

// House style lives in .editorconfig; `./gradlew ktlintCheck` enforces it and `ktlintFormat` fixes it.
subprojects {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version = ktlintVersion
    }
}
