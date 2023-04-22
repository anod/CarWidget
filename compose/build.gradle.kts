plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 29
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
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

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.activity:activity-compose:1.7.1")

    implementation(libs.kotlin.stdlib)
}