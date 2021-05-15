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
        jvmTarget = "1.8"
        useIR = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.0.0-beta01"
    }
}

dependencies {
    // AppLog, Bundle extensions
    implementation(project(":lib:framework"))

    implementation("androidx.compose.ui:ui:1.0.0-beta06")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:1.0.0-beta06")
    // Material Design
    implementation("androidx.compose.material:material:1.0.0-beta06")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.0.0-beta06")
    implementation("androidx.compose.material:material-icons-extended:1.0.0-beta06")
    // Integration with activities
    // implementation("androidx.activity:activity-compose:1.3.0-alpha07")

//    implementation("androidx.appcompat:appcompat:1.3.0-rc01")
//    implementation("androidx.activity:activity-ktx:1.2.3")
    implementation("androidx.fragment:fragment-ktx:1.3.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")

    implementation("androidx.compose.ui:ui-tooling:1.0.0-beta06")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.30")
}