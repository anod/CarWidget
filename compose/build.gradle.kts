repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 30

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
}

dependencies {
    // AppLog, Bundle extensions
    implementation(project(":lib:applog"))
    implementation(project(":lib:framework"))
    implementation(project(":content"))

    implementation("io.insert-koin:koin-core:3.0.2")

    implementation("androidx.fragment:fragment-ktx:1.3.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")

    implementation("androidx.compose.ui:ui:1.0.0-beta07")
    implementation("androidx.compose.foundation:foundation:1.0.0-beta07")
    implementation("androidx.compose.material:material:1.0.0-beta07")
    implementation("androidx.compose.material:material-icons-core:1.0.0-beta07")
    implementation("androidx.compose.material:material-icons-extended:1.0.0-beta07")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha05")

    implementation("androidx.compose.ui:ui-tooling:1.0.0-beta07")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.32")
}