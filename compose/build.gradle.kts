repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 31

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
        kotlinCompilerExtensionVersion = "1.2.0-alpha08"
    }
}

dependencies {
    // AppLog, Bundle extensions
    implementation(project(":lib:applog"))
    implementation(project(":lib:compose"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:ktx"))
    implementation(project(":content"))

    implementation("io.insert-koin:koin-core:3.2.0-beta-1")
    implementation("io.coil-kt:coil-compose-base:2.0.0-rc03")

    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0-beta01")

    implementation("androidx.compose.ui:ui:1.2.0-alpha08")
    implementation("androidx.compose.foundation:foundation:1.2.0-alpha08")
    implementation("androidx.compose.material:material:1.2.0-alpha08")
    implementation("androidx.compose.material:material-icons-core:1.2.0-alpha08")
    implementation("androidx.compose.material:material-icons-extended:1.2.0-alpha08")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.0-beta01")
    implementation("androidx.navigation:navigation-compose:2.4.2")
    implementation("androidx.activity:activity-compose:1.4.0")

    implementation("com.google.accompanist:accompanist-pager:0.24.5-alpha")
    implementation("com.google.accompanist:accompanist-flowlayout:0.24.6-alpha")

    implementation("androidx.compose.ui:ui-tooling:1.1.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")
}