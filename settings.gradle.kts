dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots")
            content {
                includeGroup("app.cash.sqldelight")
            }
        }
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots")
            content {
                includeGroup("app.cash.sqldelight")
            }
        }
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
