import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Android host for the shared component catalog in :sample. Not published.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "io.github.maniramezan.kmpcomponents.sample.android"
    compileSdk = 37
    defaultConfig {
        applicationId = "io.github.maniramezan.kmpcomponents.sample"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { compose = true }
    lint {
        // The launcher artwork is a full-color bitmap with no silhouette for a themed icon. Dependabot
        // owns version freshness.
        disable +=
            setOf("MonochromeLauncherIcon", "AndroidGradlePluginVersion", "GradleDependency", "NewerVersionAvailable")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin { compilerOptions.jvmTarget = JvmTarget.JVM_17 }

dependencies {
    implementation(project(":sample"))
    implementation(libs.androidx.activity.compose)
}
