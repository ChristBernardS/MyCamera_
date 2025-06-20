// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}

// Define versions as top-level properties for Kotlin DSL
val agpVersion = "8.10.3" // Android Gradle Plugin version
val kotlinVersion = "2.0.21" // Kotlin version
val googleServicesVersion = "4.4.1" // Google Services plugin version

// Versi Compose dan Navigation Compose
//val composeVersion = "1.4.3"
val composeCompilerVersion = "2.0.21" // Harus cocok dengan versi Kotlin Anda
val composeBomVersion = "2025.06.01" // Versi Compose BOM (Bill of Materials)
