plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.hypersoft.admobpreloader"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24
    }

    buildTypes {
        debug {
            resValue("string", "admob_app_open_id", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "admob_banner_entrance_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_on_boarding_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_dashboard_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_one_a_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_one_b_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_two_a_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_two_b_id", "ca-app-pub-3940256099942544/2014213617")

            resValue("string", "admob_inter_entrance_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_on_boarding_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_dashboard_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_bottom_navigation_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_back_press_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_exit_id", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_on_boarding_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_home_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_feature_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_exit_id", "ca-app-pub-3940256099942544/2247696110")

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        release {
            resValue("string", "admob_app_open_id", "ca-app-pub-3940256099942544/9257395921")

            resValue("string", "admob_banner_entrance_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_on_boarding_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_dashboard_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_one_a_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_one_b_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_two_a_id", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "admob_banner_feature_two_b_id", "ca-app-pub-3940256099942544/2014213617")

            resValue("string", "admob_inter_entrance_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_on_boarding_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_dashboard_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_bottom_navigation_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_back_press_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admob_inter_exit_id", "ca-app-pub-3940256099942544/1033173712")

            resValue("string", "admob_native_language_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_on_boarding_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_home_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_feature_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_native_exit_id", "ca-app-pub-3940256099942544/2247696110")

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
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // GMS (NextGen)
    implementation(libs.ads.mobile.sdk)

    // Koin
    implementation(libs.koin.android)
}