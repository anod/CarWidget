buildscript {
    val kotlinVersion by extra("1.4.30")
    repositories {
        google()
        mavenCentral()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha10")
        classpath(kotlin("gradle-plugin", version = "$kotlinVersion"))
    }
}
