plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.multiplatform.android.library)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidLibrary {
        namespace = "info.anodsplace.carwidget.iconpack"
        compileSdk = 36
        minSdk = 31
        androidResources {
            enable = true
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                implementation(project(":lib:compose"))
                implementation(libs.capturable)
                implementation(libs.accompanist.drawablepainter)
            }
        }
    }
}