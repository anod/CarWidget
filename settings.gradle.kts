dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.7.20")
            library("kotlin-gradle-plugin", "org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlin")
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")

            version("coroutines", "1.6.4")
            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutines")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")

            version("sqldelight", "1.5.5")
            library("sqldelight-driver-android", "com.squareup.sqldelight", "android-driver").versionRef("sqldelight")
            library("sqldelight-coroutines-extensions-jvm", "com.squareup.sqldelight", "coroutines-extensions-jvm").versionRef("sqldelight")
            library("sqldelight-gradle-plugin", "com.squareup.sqldelight", "gradle-plugin").versionRef("sqldelight")

        }
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
