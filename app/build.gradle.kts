
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

    implementation("io.insert-koin:koin-core:3.0.2")
    implementation("com.squareup.picasso:picasso:2.8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")

    // Activity recognition
    implementation("com.google.android.gms:play-services-location:18.0.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.core:core-ktx:1.5.0")
    implementation("androidx.activity:activity:1.2.3")
    implementation("androidx.fragment:fragment:1.3.4")
    implementation("androidx.fragment:fragment-ktx:1.3.4")
    implementation("androidx.collection:collection-ktx:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.sqlite:sqlite-ktx:2.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.3.1")
    kapt("androidx.lifecycle:lifecycle-compiler:2.3.1")

    implementation("ch.acra:acra-core:5.7.0")
    implementation("ch.acra:acra-notification:5.7.0")
    implementation("ch.acra:acra-limiter:5.7.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.10")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
}

android {
    // compileSdk = 30
    compileSdkPreview = "android-S"

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        minSdk = 29
        //targetSdk = 31 // 29 wifi switch not working
        targetSdkPreview = "S"
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

    flavorDimensions("tier")

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
        warning("InvalidFragmentVersionForActivityResult")
    }
}


