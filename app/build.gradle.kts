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

    implementation(libs.koin.core)
    implementation(libs.coil.compose.base)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Activity recognition
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("androidx.appcompat:appcompat:1.6.1") // AppCompatActivity
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.activity:activity:1.7.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.collection:collection-ktx:1.2.0")
    implementation("androidx.sqlite:sqlite-ktx:2.3.1")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.1")

    implementation("ch.acra:acra-core:5.9.7")
    implementation("ch.acra:acra-notification:5.9.7")
    implementation("ch.acra:acra-limiter:5.9.7")

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
        applicationId = "com.anod.car.home.free"

        versionCode = 22000
        versionName = "2.2.0"
        vectorDrawables.generatedDensities("hdpi", "xxhdpi")
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.keystore")
        }
        create("release") {
            storeFile = file(findProperty("CARWIDGET_KEYSTORE_FILE") ?: ".")
            storePassword = findProperty("CARWIDGET_KEYSTORE_PASSWORD") as? String
            keyAlias = findProperty("CARWIDGET_KEY_ALIAS") as? String
            keyPassword = findProperty("CARWIDGET_KEY_PASSWORD") as? String
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

// https://youtrack.jetbrains.com/issue/KT-55947
kotlin {
    jvmToolchain(17)
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions.jvmTarget = "11"
}