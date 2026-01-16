plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.multiplatform.android.library)
    alias(libs.plugins.sqldelight)
}

kotlin {

    androidLibrary {
        compileSdk = 36
        namespace = "info.anodsplace.carwidget.content"

        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain {
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
            androidUnitTest {
                dependencies {
                    implementation(libs.kotlin.test)
                    implementation(libs.sqldelight.driver.sqlite)
                    implementation(libs.robolectric)
                    implementation(libs.androidx.test.core)
                }
            }
        }
    }
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