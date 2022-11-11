
allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io" )
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        google()
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0-alpha08")
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.sqldelight.gradle.plugin)
    }
}