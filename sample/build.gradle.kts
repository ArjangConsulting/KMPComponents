import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Catalog of every component, shared by the desktop app (`./gradlew :sample:run`), the Android app
// (`:sample-android`), and the iOS app (`sample/ios`). Not published.
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm()
    android {
        namespace = "io.github.maniramezan.kmpcomponents.sample.shared"
        compileSdk = 37
        minSdk = 26
        compilerOptions.jvmTarget = JvmTarget.JVM_17
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "CatalogKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":components"))
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.maniramezan.kmpcomponents.sample.MainKt"
    }
}
