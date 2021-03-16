repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkVersion(30)

    defaultConfig {
        minSdkVersion(29)
    }

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation(project(":lib:framework"))

    implementation("androidx.compose.ui:ui:1.0.0-beta02")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:1.0.0-beta02")
    // Material Design
    implementation("androidx.compose.material:material:1.0.0-beta02")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.0.0-beta02")
    implementation("androidx.compose.material:material-icons-extended:1.0.0-beta02")
    // Tooling support (Previews, etc.)
    implementation("androidx.ui:ui-tooling:1.0.0-alpha07")
    // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:1.0.0-beta02")
    // Integration with activities
    implementation("androidx.activity:activity-compose:1.3.0-alpha04")

    implementation("androidx.appcompat:appcompat:1.3.0-beta01")
    implementation("androidx.activity:activity-ktx:1.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.0")
}