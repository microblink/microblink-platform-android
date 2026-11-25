plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.microblink.platform.sample"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.microblink.platform.sample"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    // The BOM sets the versions of the SDK and of the scanner SDKs it runs.
    implementation(platform(libs.microblink.platform.bom))
    implementation(libs.microblink.platform)
    // Add only the scanner SDKs your workflows use: a step whose SDK is missing fails with
    // EngineError.MissingScannerSdk.
    implementation(libs.blinkid.verify.ux) // document scanning
    implementation(libs.blinkcard.ux) // card scanning
    implementation(libs.biometrics.ux) // face capture

    // Creates the transaction in the app, for testing only. See TransactionClient.kt.
    implementation(project(":transaction-client"))
}