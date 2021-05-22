repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
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
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.collection:collection-ktx:1.1.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.32")
}