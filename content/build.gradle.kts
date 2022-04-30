repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("android")
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 29
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":lib:applog"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:ktx"))
    implementation("io.insert-koin:koin-core:3.1.6")
    implementation("io.coil-kt:coil-base:2.0.0-rc03")

    implementation("com.squareup.sqldelight:android-driver:1.5.3")
    implementation("com.squareup.sqldelight:coroutines-extensions-jvm:1.5.3")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.collection:collection-ktx:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")
}