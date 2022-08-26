repositories {
    mavenCentral()
    google()
}

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
        kotlinCompilerExtensionVersion = "1.3.0-rc01"
    }
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

    implementation("io.insert-koin:koin-core:3.2.0")
    implementation("io.coil-kt:coil-compose-base:2.2.0")

    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("androidx.fragment:fragment-ktx:1.5.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0-alpha01")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0-alpha01")

    implementation("com.google.android.material:material:1.6.1")

    implementation("androidx.compose.ui:ui:1.3.0-alpha03")
    implementation("androidx.compose.foundation:foundation:1.3.0-alpha03")
    implementation("androidx.compose.material:material-icons-core:1.3.0-alpha03")
    implementation("androidx.compose.material:material-icons-extended:1.3.0-alpha03")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0-alpha01")
    implementation("androidx.navigation:navigation-compose:2.5.1")
    implementation("androidx.activity:activity-compose:1.5.1")

    implementation("com.google.accompanist:accompanist-pager:0.25.1")
    implementation("com.google.accompanist:accompanist-flowlayout:0.25.1")

    implementation("androidx.compose.ui:ui-tooling:1.2.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
}