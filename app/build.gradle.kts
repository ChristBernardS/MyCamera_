plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.mycamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mycamera"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view) // Atau versi terbaru yang stabil

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.extensions)


    implementation(platform(libs.androidx.compose.bom.v20250601))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3) // Material Design 3
    implementation(libs.androidx.activity.compose.v182) // Activity Compose

    implementation(libs.androidx.material.icons.extended)
    implementation(libs.material.icons.extended)

    // Compose Navigation
    implementation(libs.androidx.navigation.compose) // Ganti dengan versi terbaru

    // Coil untuk memuat gambar dari URL
    implementation(libs.coil.compose) // Ganti dengan versi terbaru

    // Firebase (untuk otentikasi dan database - akan digunakan nanti)
    implementation(platform(libs.firebase.bom)) // Ganti dengan versi terbaru Firebase BOM
    implementation(libs.firebase.auth.ktx) // Autentikasi Firebase
    implementation(libs.firebase.firestore.ktx) // Firestore Database

    implementation(libs.play.services.auth)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)

    // Icon (Opsional: Jika Anda menggunakan ikon yang tidak ada di Material Icons)
    // Misalnya, untuk Phosphor Icons
    // implementation("com.phosphoricons.compose:phosphor-icons-compose:1.0.0-beta06")

    // Pengujian
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20250601))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)



}