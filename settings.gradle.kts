pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Kotlin/Wasm downloads Node.js and Yarn to run the web tests, and Binaryen to optimize the
        // test executable. FAIL_ON_PROJECT_REPOS rejects
        // the repositories the Kotlin plugin would add itself, so they are declared here and the
        // plugin's own download URLs are cleared in the root build script.
        ivy("https://nodejs.org/dist") {
            name = "Node.js distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("org.nodejs", "node") }
        }
        ivy("https://github.com/yarnpkg/yarn/releases/download") {
            name = "Yarn distributions"
            patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.yarnpkg", "yarn") }
        }
        ivy("https://github.com/WebAssembly/binaryen/releases/download") {
            name = "Binaryen distributions"
            patternLayout { artifact("version_[revision]/binaryen-version_[revision]-[classifier].[ext]") }
            metadataSources { artifact() }
            content { includeModule("com.github.webassembly", "binaryen") }
        }
    }
}

if (providers.gradleProperty("useLocalKommon").orNull.toBoolean()) {
    // `-PkommonDir=<path>` points at a checkout other than the sibling one, such as a git worktree.
    val localKommon = file(providers.gradleProperty("kommonDir").orElse("../kommon").get())
    require(localKommon.resolve("settings.gradle.kts").isFile) {
        "useLocalKommon requires a kommon checkout at $localKommon (override with -PkommonDir=<path>)"
    }
    includeBuild(localKommon)
}

rootProject.name = "KMPComponents"
include(":components")
include(":sample")
include(":sample-android")
