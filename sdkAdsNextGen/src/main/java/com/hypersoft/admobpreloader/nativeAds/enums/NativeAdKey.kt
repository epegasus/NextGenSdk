package com.hypersoft.admobpreloader.nativeAds.enums

/**
 * Mirrors interstitial's InterAdKey but for native placements.
 */
enum class NativeAdKey(val value: String) {
    LANGUAGE("language"),
    ON_BOARDING("onBoarding"),
    DASHBOARD("dashboard"),
    FEATURE("feature"),
    EXIT("exit")
}