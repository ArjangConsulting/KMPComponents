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

val localKommon = listOf(file("../kommon"), file("kommon")).firstOrNull {
    it.resolve("settings.gradle.kts").isFile
}
if (localKommon != null) includeBuild(localKommon)

rootProject.name = "KMPComponents"
include(":components")
