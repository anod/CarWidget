plugins {
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("android")
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 29
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
    buildFeatures {
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "info.anodsplace.carwidget.content"
}

sqldelight {
    database("Database") {
        packageName = "info.anodsplace.carwidget.content.db"
        sourceFolders = listOf("sqldelight")
        schemaOutputDirectory = file("src/main/sqldelight/schema")
    }
}

dependencies {
    implementation(project(":lib:applog"))
    implementation(project(":lib:graphics"))
    implementation(project(":lib:ktx"))

    implementation(libs.koin.core)
    implementation(libs.coil.compose.base)

    implementation(libs.sqldelight.driver.android)
    implementation(libs.sqldelight.coroutines.extensions.jvm)
    implementation(libs.preference.ktx) // for androidx.preference.PreferenceManager
    implementation(libs.core.ktx)
    implementation(libs.collection.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.kotlin.stdlib)
}