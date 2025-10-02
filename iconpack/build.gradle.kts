import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions { jvmTarget = JvmTarget.JVM_11 }
}

android {
    namespace = "info.anodsplace.carwidget.iconpack"
    compileSdk = 36
    defaultConfig { minSdk = 31 }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.capturable)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.accompanist.drawablepainter)

    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.core)
    api(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
}
