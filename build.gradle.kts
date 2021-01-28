buildscript {
    val kotlinVersion by extra("1.4.21")
    repositories {
        jcenter()
        mavenCentral()
        google()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-alpha05")
        classpath(kotlin("gradle-plugin", version = "1.4.21-2"))
    }
}
