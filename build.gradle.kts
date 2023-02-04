
allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0-beta01")
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.sqldelight.gradle.plugin)
    }
}