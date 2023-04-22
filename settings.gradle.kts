dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.8.20")
            library("kotlin-gradle-plugin", "org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlin")
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
            plugin("kotlin-plugin","org.jetbrains.kotlin.android").versionRef("kotlin")

            version("coroutines", "1.6.4")
            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutines")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")

            version("sqldelight", "1.5.5")
            library("sqldelight-driver-android", "com.squareup.sqldelight", "android-driver").versionRef("sqldelight")
            library("sqldelight-coroutines-extensions-jvm", "com.squareup.sqldelight", "coroutines-extensions-jvm").versionRef("sqldelight")
            library("sqldelight-gradle-plugin", "com.squareup.sqldelight", "gradle-plugin").versionRef("sqldelight")
            plugin("sqldelight-plugin","com.squareup.sqldelight").versionRef("sqldelight")

            library("koin-core", "io.insert-koin", "koin-core").version("3.4.0")
            library("coil-compose-base", "io.coil-kt", "coil-compose-base").version("2.3.0")
        }
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
    ":lib:compose",
    ":lib:graphics",
    ":lib:framework",
    ":lib:ktx",
    ":lib:permissions",
    ":lib:viewmodel"
)
