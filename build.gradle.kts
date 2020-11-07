buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.0")
        classpath(kotlin("gradle-plugin", version = "1.4.10"))
    }
}
