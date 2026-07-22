plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.multiplatform.android.library)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        namespace = "info.anodsplace.carwidget.iconpack"
        compileSdk = 37
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