plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "proyects.camachopichal.apps.anotherweatherapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "proyects.camachopichal.apps.anotherweatherapp"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        viewBinding = true
    }
    buildToolsVersion = "36.0.0"
}

dependencies {
    // Core dependencies
    implementation(libs.appcompat)
    implementation(libs.material)

    // Servicio de geolocalizacion
    implementation(libs.play.services.location)

    // Implementación principal de Glide, para cargar las imagenes en la interfaz
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // Test dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}