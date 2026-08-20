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
    }
}

if (providers.gradleProperty("useLocalKommon").orNull.toBoolean()) {
    val localKommon = file("../kommon")
    require(localKommon.resolve("settings.gradle.kts").isFile) {
        "useLocalKommon requires kommon to be checked out next to KMPComponents"
    }
    includeBuild(localKommon)
}

rootProject.name = "KMPComponents"
include(":components")
