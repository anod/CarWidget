plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.multiplatform.android.library)
}

kotlin {
    androidLibrary {
        namespace = "info.anodsplace.carwidget.skin"
        compileSdk = 36
        minSdk = 31
        androidResources {
            enable = true
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                // AppLog, Bundle extensions
                implementation(project(":content"))
                implementation(project(":lib:applog"))
                implementation(libs.androidx.palette.ktx)
                implementation(libs.material) // Theme.Material3.DynamicColors.DayNight
            }
        }
    }
}