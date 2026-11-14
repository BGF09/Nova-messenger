plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    androidTarget()
    jvm()
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation("io.ktor:ktor-client-core:3.3.3")
            implementation("io.ktor:ktor-client-cio:3.3.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
    }
}

android {
    namespace = "com.nullmessenger.shared"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
}
