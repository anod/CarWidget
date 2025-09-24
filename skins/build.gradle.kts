import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    namespace = "info.anodsplace.carwidget.skin"
}

dependencies {
    // AppLog, Bundle extensions
    implementation(project(":content"))
    implementation(project(":lib:applog"))
    implementation(libs.androidx.palette.ktx)
    implementation(libs.material) // Theme.Material3.DynamicColors.DayNight
}