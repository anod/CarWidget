
repositories {
    mavenCentral()
    maven(url = "https://jitpack.io" )
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    google()
}

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

dependencies {
    implementation(project(":content"))
    implementation(project(":compose"))
    implementation(project(":lib:applog"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:colorpicker"))

    implementation("io.insert-koin:koin-core:3.1.6")
    implementation("io.coil-kt:coil-base:2.0.0-rc03")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    // Activity recognition
    implementation("com.google.android.gms:play-services-location:19.0.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.activity:activity:1.4.0")
    implementation("androidx.fragment:fragment:1.4.1")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.collection:collection-ktx:1.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.1")
    implementation("androidx.sqlite:sqlite-ktx:2.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.1")
    kapt("androidx.lifecycle:lifecycle-common-java8:2.4.1")

    implementation("ch.acra:acra-core:5.8.4")
    implementation("ch.acra:acra-notification:5.8.4")
    implementation("ch.acra:acra-limiter:5.8.4")

    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    kapt("com.google.auto.service:auto-service:1.0.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")
}

android {
    compileSdk = 31

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        minSdk = 29
        targetSdk = 31 // 29 wifi switch not working

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
}