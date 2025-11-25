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

            resValue("string", "admob_app_open_id", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "admob_banner_entrance_id", "ca-app-pub-3940256099942544/2014213617")

            resValue("string", "admob_inter_entrance_id", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")


            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        release {
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "admob_app_open_id", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "admob_banner_entrance_id", "ca-app-pub-3940256099942544/2014213617")

            resValue("string", "admob_inter_entrance_id", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")


            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // GMS (NextGen)
    implementation(libs.ads.mobile.sdk)
    //implementation(libs.play.services.ads)

    // Koin
    implementation(libs.koin.android)
}