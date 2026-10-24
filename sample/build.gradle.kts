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
            implementation(project(":components-image"))
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.maniramezan.kmpcomponents.sample.MainKt"
        jvmArgs += "--enable-native-access=ALL-UNNAMED"
        if (System.getProperty("os.name").startsWith("Mac")) {
            jvmArgs += "-Xdock:name=KMPComponents"
        }
        nativeDistributions {
            packageName = "KMPComponents"
            macOS { iconFile.set(project.file("icons/app-icon.icns")) }
            windows { iconFile.set(project.file("icons/app-icon.ico")) }
            linux { iconFile.set(project.file("src/jvmMain/resources/app-icon.png")) }
        }
    }
}
