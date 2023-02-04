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
        kotlinCompilerExtensionVersion = "1.3.2"
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

    implementation("io.insert-koin:koin-core:3.3.2")
    implementation("io.coil-kt:coil-compose-base:2.2.2")

    implementation("androidx.appcompat:appcompat:1.6.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0-alpha04")

    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.activity:activity-compose:1.6.1")

    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.28.0")

    implementation(libs.kotlin.stdlib)
}