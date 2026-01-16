plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.multiplatform.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidLibrary {
        namespace = "info.anodsplace.carwidget"
        compileSdk = 36
        minSdk = 31
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain {
            dependencies {
                // AppLog, Bundle extensions
                implementation(project(":lib:applog"))
                implementation(project(":lib:compose"))
                implementation(project(":lib:framework"))
                implementation(project(":lib:graphics"))
                implementation(project(":lib:permissions"))
                implementation(project(":lib:ktx"))
                implementation(project(":lib:viewmodel"))
                implementation(project(":content"))
                implementation(project(":skins"))
                implementation(project(":iconpack"))

                implementation(libs.koin.core)
                implementation(libs.coil.compose.base)

                implementation(libs.appcompat)

                implementation(libs.androidx.navigation3.ui)
                implementation(libs.androidx.navigation3.runtime)
                implementation(libs.androidx.lifecycle.viewmodel.navigation3)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.androidx.activity)
            }
        }
        androidUnitTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.robolectric)
            }
        }
    }
}

