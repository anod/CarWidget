repositories {
    mavenCentral()
    jcenter()
    google()
}

plugins {
    id("com.android.library")
    id("kotlin-android")
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
        kotlinCompilerVersion = "1.4.10"
        kotlinCompilerExtensionVersion = "1.0.0-alpha04"
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.20")
    implementation(project(":lib:framework"))
    implementation("androidx.appcompat:appcompat:1.3.0-alpha02")
    implementation("androidx.activity:activity-ktx:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")

    implementation("androidx.compose.ui:ui:1.0.0-alpha09")
    // Tooling support (Previews, etc.)
    implementation("androidx.ui:ui-tooling:1.0.0-alpha07")
    // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
    implementation("androidx.compose.foundation:foundation:1.0.0-alpha09")
    // Material Design
    implementation("androidx.compose.material:material:1.0.0-alpha09")
    // Material design icons
    implementation("androidx.compose.material:material-icons-core:1.0.0-alpha09")
    implementation("androidx.compose.material:material-icons-extended:1.0.0-alpha09")
    // Integration with observables
    implementation("androidx.compose.runtime:runtime-livedata:1.0.0-alpha09")
}