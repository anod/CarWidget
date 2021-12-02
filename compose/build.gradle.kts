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
        kotlinCompilerExtensionVersion = "1.1.0-beta04"
    }
}

dependencies {
    // AppLog, Bundle extensions
    implementation(project(":lib:applog"))
    implementation(project(":lib:compose"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:graphics"))
    implementation(project(":content"))

    implementation("io.insert-koin:koin-core:3.1.4")
    implementation("com.squareup.picasso:picasso:2.8")

    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.4.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")

    implementation("androidx.compose.ui:ui:1.1.0-beta04")
    implementation("androidx.compose.foundation:foundation:1.1.0-beta04")
    implementation("androidx.compose.material:material:1.1.0-beta04")
    implementation("androidx.compose.material:material-icons-core:1.1.0-beta04")
    implementation("androidx.compose.material:material-icons-extended:1.1.0-beta04")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.0")
    implementation("androidx.navigation:navigation-compose:2.4.0-beta02")
    implementation("androidx.activity:activity-compose:1.4.0")

    implementation("com.google.accompanist:accompanist-pager:0.20.0")
    implementation("com.google.accompanist:accompanist-flowlayout:0.20.0")

    implementation("androidx.compose.ui:ui-tooling:1.0.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0")
}