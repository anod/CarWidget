dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(
    ":app",
    ":compose",
    ":content",
    ":skins",
    ":lib:applog",
    ":lib:graphics",
    ":lib:context",
    ":lib:framework",
    ":lib:permissions",
    ":lib:ktx",
    ":lib:compose",
    ":lib:viewmodel",
    ":iconpack"
)
include(":baselineprofile")
