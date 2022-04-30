buildscript {
    val kotlinVersion by extra("1.6.20")
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.0-rc01")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
    }
}