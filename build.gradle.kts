@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.roborazzi) apply false
}

allprojects {
    group = "io.github.maniramezan.kmpcomponents"
    version = providers.gradleProperty("VERSION_NAME").orElse("0.0.0-SNAPSHOT").get()

    // The Node.js, Yarn, and Binaryen repositories live in settings.gradle.kts (see the note there); a null base
    // URL stops the Kotlin plugin from adding them to the project, which FAIL_ON_PROJECT_REPOS rejects.
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsEnvSpec>().downloadBaseUrl.set(null as String?)
    }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootEnvSpec>().downloadBaseUrl.set(null as String?)
    }
    plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenPlugin> {
        the<org.jetbrains.kotlin.gradle.targets.wasm.binaryen.BinaryenEnvSpec>().downloadBaseUrl.set(null as String?)
    }
}

// One API reference site for every published module: `./gradlew :dokkaGenerate` writes
// build/dokka/html.
dependencies {
    dokka(project(":components"))
    dokka(project(":components-image"))
}

dokka {
    moduleName = "KMPComponents"
}

// Each module's reference opens with its Module.md overview and links declarations to their source
// on GitHub, at the release tag when docs.yml passes VERSION_NAME and at main otherwise.
val sourceRef: Provider<String> = providers.gradleProperty("VERSION_NAME").orElse("main")
subprojects {
    val moduleDir = name
    plugins.withId("org.jetbrains.dokka") {
        extensions.configure<org.jetbrains.dokka.gradle.DokkaExtension> {
            dokkaSourceSets.configureEach {
                includes.from(layout.projectDirectory.file("Module.md"))
                sourceLink {
                    localDirectory.set(layout.projectDirectory.dir("src"))
                    remoteUrl(sourceRef.map { "https://github.com/ArjangConsulting/KMPComponents/tree/$it/$moduleDir/src" })
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }
}

val ktlintVersion = libs.versions.ktlint.get()

// House style lives in .editorconfig; `./gradlew ktlintCheck` enforces it and `ktlintFormat` fixes it.
subprojects {
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version = ktlintVersion
    }
}
