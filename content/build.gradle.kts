import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

android {
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    namespace = "info.anodsplace.carwidget.content"
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("info.anodsplace.carwidget.content.db")
            srcDirs("src/main/sqldelight")
            schemaOutputDirectory.set(file("src/main/sqldelight/schema"))
            verifyMigrations.set(false) // migrations not handle autoincrement and unique index
            deriveSchemaFromMigrations.set(false)
        }
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
    implementation(libs.sqldelight.primitive.adapters)
    implementation(libs.preference.ktx) // for androidx.preference.PreferenceManager
    implementation(libs.androidx.core.ktx)
    implementation(libs.collection.ktx)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}