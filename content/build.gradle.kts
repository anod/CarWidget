plugins {
    id("com.android.library")
    id("com.squareup.sqldelight")
    kotlin("android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 29
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
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
    implementation("io.insert-koin:koin-core:3.3.2")
    implementation("io.coil-kt:coil-base:2.2.2")

    implementation(libs.sqldelight.driver.android)
    implementation(libs.sqldelight.coroutines.extensions.jvm)
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.collection:collection-ktx:1.2.0")
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.kotlin.stdlib)
}