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

if (file("../kommon/settings.gradle.kts").isFile) {
    includeBuild("../kommon")
}

rootProject.name = "KMPComponents"
include(":components")
