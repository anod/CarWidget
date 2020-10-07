buildscript {
    val kotlinVersion by extra("1.4.10")
    repositories {
        jcenter()
        mavenCentral()
        google()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha13")
        classpath(kotlin("gradle-plugin", version = "1.4.10"))
    }
}
