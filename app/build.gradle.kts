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
            resValue("string", "admob_inter_on_boarding_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_dashboard_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_bottom_navigation_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_back_press_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_exit_id", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")


            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        release {
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "admob_app_open_id", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "admob_banner_entrance_id", "ca-app-pub-3940256099942544/2014213617")

            resValue("string", "admob_inter_entrance_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_on_boarding_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_dashboard_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_bottom_navigation_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_back_press_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_exit_id", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")


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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

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
    implementation(libs.androidx.fragment)
}