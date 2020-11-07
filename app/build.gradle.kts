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
    kotlin("kapt")
    id("kotlin-android")
}

dependencies {
    implementation(project(":compose"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:colorpicker"))

    implementation("com.squareup.picasso:picasso:2.71828")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.0")

    // Activity recognition
    implementation("com.google.android.gms:play-services-location:17.0.0")

    implementation("com.google.android.material:material:1.3.0-alpha03")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.2.5")
    implementation("androidx.collection:collection-ktx:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.sqlite:sqlite-ktx:2.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.2.0")
    kapt("androidx.lifecycle:lifecycle-compiler:2.2.0")

    implementation("androidx.work:work-runtime:2.4.0")
    implementation("androidx.work:work-runtime-ktx:2.4.0")

    implementation("ch.acra:acra-core:5.7.0")
    implementation("ch.acra:acra-notification:5.7.0")
    implementation("ch.acra:acra-limiter:5.7.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.10")
    implementation("com.android.support.constraint:constraint-layout:2.0.4")
}

android {
    compileSdkVersion(29)

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(28) // 29 wifi switch not working
        versionCode = 21500
        versionName = "2.1.5"
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
        jvmTarget = "1.8"
        useIR = true
    }
}


