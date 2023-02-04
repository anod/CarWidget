plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
    namespace = "info.anodsplace.carwidget.skin"
}

dependencies {
    // AppLog, Bundle extensions
    implementation(project(":content"))
    implementation(project(":lib:applog"))
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation(libs.kotlin.stdlib)
}