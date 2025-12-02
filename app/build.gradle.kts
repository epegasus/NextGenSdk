plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.pegasus.nextgensdk"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "dev.pegasus.nextgensdk"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        debug {
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        release {
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":sdkAdsNextGen"))
    implementation(project(":core"))

    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Fragment
    implementation(libs.androidx.fragment)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigational Components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // GMS (NextGen)
    implementation(libs.ads.mobile.sdk)
    //implementation(libs.play.services.ads)

    // Koin
    implementation(libs.koin.android)
}