import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    namespace = "info.anodsplace.carwidget"
}

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

    implementation(libs.koin.core)
    implementation(libs.coil.compose.base)

    implementation(libs.appcompat)

    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.navigation.compose)
    implementation(libs.androidx.activity.compose)
}