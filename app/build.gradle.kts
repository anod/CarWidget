plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

dependencies {
    implementation(project(":content"))
    implementation(project(":compose"))
    implementation(project(":skins"))
    implementation(project(":lib:applog"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:permissions"))

    implementation("io.insert-koin:koin-core:3.3.2")
    implementation("io.coil-kt:coil-base:2.2.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Activity recognition
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("androidx.appcompat:appcompat:1.6.0") // AppCompatActivity
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.activity:activity:1.6.1")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("androidx.collection:collection-ktx:1.2.0")
    implementation("androidx.sqlite:sqlite-ktx:2.3.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.5.1")

    implementation("ch.acra:acra-core:5.9.6")
    implementation("ch.acra:acra-notification:5.9.6")
    implementation("ch.acra:acra-limiter:5.9.6")

    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")

    implementation(libs.kotlin.stdlib)
}

android {
    compileSdk = 33

    buildFeatures {
        aidl = true
        buildConfig = true
    }

    defaultConfig {
        minSdk = 29
        targetSdk = 33 // 29 wifi switch not working

        versionCode = 22000
        versionName = "2.2.0"
        vectorDrawables.generatedDensities("hdpi", "xxhdpi")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.keystore")
        }
        create("release") {
            storeFile = file(***REMOVED***)
            storePassword = ***REMOVED***
            keyAlias  = ***REMOVED***
            keyPassword = ***REMOVED***
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    flavorDimensions += listOf("tier")

    productFlavors {
        create("pro") {
            applicationId = "com.anod.car.home.pro"
            dimension = "tier"
        }
        create("free") {
            applicationId = "com.anod.car.home.free"
            dimension = "tier"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }


    lint {
        warning.add("InvalidFragmentVersionForActivityResult")
    }
    namespace = "com.anod.car.home"
}