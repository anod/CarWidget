repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://jitpack.io" )
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    google()
}

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}

dependencies {
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.2.0-alpha03")
    implementation("androidx.preference:preference:1.1.0")
    implementation("androidx.palette:palette:1.0.0")

    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.exifinterface:exifinterface:1.1.0-beta01")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.3")

    // Activity recognition
    implementation("com.google.android.gms:play-services-location:17.0.0")

    implementation("androidx.core:core-ktx:1.2.0-rc01")
    implementation("androidx.fragment:fragment-ktx:1.2.0-rc04")
    implementation("androidx.collection:collection-ktx:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0-rc03")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0-rc03")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.sqlite:sqlite-ktx:2.0.1")

    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0-rc03")
    kapt("androidx.lifecycle:lifecycle-compiler:2.2.0-rc03")

    implementation("ch.acra:acra-core:5.3.0")
    implementation("ch.acra:acra-notification:5.3.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61")
    implementation("com.android.support.constraint:constraint-layout:2.0.0-beta4")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 20600
        versionName = "2.0.6"
        vectorDrawables.generatedDensities("hdpi","xxhdpi")
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
            isShrinkResources = false
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions("tier")

    productFlavors {
        create("pro") {
            applicationId = "com.anod.car.home.pro"
            setDimension("tier")
        }
        create("free") {
            applicationId = "com.anod.car.home.free"
            setDimension("tier")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        this.let {
            it.jvmTarget = "1.8"
        }
    }
}

