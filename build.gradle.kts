plugins {
    id("com.github.ben-manes.versions") version "0.58.0"
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatform.android.library) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.compose.compiler) apply false
}

subprojects {
    // Robolectric 4.17 on JDK 17+ reflects into jdk.internal.access (FileDescriptor shadow at API 37+).
    // The JPMS blocks this by default, so open the package to the forked unit-test JVM.
    tasks.withType<Test>().configureEach {
        jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
    }
}
