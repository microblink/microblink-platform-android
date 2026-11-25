plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.microblink.platform.sample.customui"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.microblink.platform.sample.customui"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)

    // The engine alone: this app draws its own screens instead of using the drop-in UI.
    implementation(platform(libs.microblink.platform.bom))
    implementation(libs.microblink.platform.engine)
    // Add only the scanner SDKs your workflows use: a step whose SDK is missing fails with
    // EngineError.MissingScannerSdk.
    implementation(libs.blinkid.verify.ux) // document scanning
    implementation(libs.blinkcard.ux) // card scanning
    implementation(libs.biometrics.ux) // face capture

    // Creates the transaction in the app, for testing only. See TransactionClient.kt.
    implementation(project(":transaction-client"))

    testImplementation(libs.junit)
}
