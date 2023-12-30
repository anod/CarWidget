plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(project(":content"))
    implementation(project(":compose"))
    implementation(project(":skins"))
    implementation(project(":lib:applog"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:context"))
    implementation(project(":lib:framework"))
    implementation(project(":lib:permissions"))

    implementation(libs.koin.core)
    implementation(libs.coil.compose.base)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Activity recognition
    implementation(libs.play.services.location)

    implementation(libs.appcompat) // AppCompatActivity
    implementation(libs.androidx.core.ktx)
    implementation(libs.activity)
    implementation(libs.core.splashscreen)
    implementation(libs.collection.ktx)
    implementation(libs.sqlite.ktx)

    // ViewModel
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.common.java8)

    implementation(libs.acra.core)
    implementation(libs.acra.notification)
    implementation(libs.acra.limiter)

    compileOnly(libs.auto.service.annotations)
    ksp(libs.auto.service.ksp)
    ksp(libs.auto.service)

    implementation(libs.kotlin.stdlib)
}

android {
    compileSdk = 34

    buildFeatures {
        aidl = true
        buildConfig = true
    }

    defaultConfig {
        minSdk = 29
        targetSdk = 33 // 29 wifi switch not working
        applicationId = "com.anod.car.home.free"

        versionCode = 33101
        versionName = "3.0.1"
    }

    androidResources {
        generateLocaleConfig = true
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