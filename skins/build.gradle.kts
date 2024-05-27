plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
}

android {
    compileSdk = 34

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
    implementation(libs.androidx.palette.ktx)
    implementation(libs.material) // Theme.Material3.DynamicColors.DayNight
    implementation(libs.kotlin.stdlib)
}