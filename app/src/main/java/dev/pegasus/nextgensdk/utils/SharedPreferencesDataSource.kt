package dev.pegasus.nextgensdk.utils

import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferencesDataSource(private val sharedPreferences: SharedPreferences) {

    private val billingRequireKey = "isAppPurchased"
    private val isShowFirstScreenKey = "showFirstScreen"

    /**
     *  ------------------- Billing -------------------
     */
    var isAppPurchased: Boolean
        get() = sharedPreferences.getBoolean(billingRequireKey, false)
        set(value) = sharedPreferences.edit { putBoolean(billingRequireKey, value) }

    /**
     *  ------------------- UI -------------------
     */
    var showFirstScreen: Boolean
        get() = sharedPreferences.getBoolean(isShowFirstScreenKey, true)
        set(value) = sharedPreferences.edit { putBoolean(isShowFirstScreenKey, value) }

    /* ---------------------------------------- Ads ---------------------------------------- */

    val appOpen = "appOpen"
    val appOpenSplash = "appOpenSplash"

    val bannerHome = "bannerHome"

    val interEntrance = "interEntrance"
    val interOnBoarding = "interOnBoarding"

    val nativeLanguage = "nativeLanguage"
    val nativeOnBoarding = "nativeOnBoarding"
    val nativeFeature = "nativeFeature"
    val nativeHome = "nativeHome"
    val nativeExit = "nativeExit"

    val rewardedAiFeature = "rewardedAiFeature"
    val rewardedInterAiFeature = "rewardedInterAiFeature"

    /**
     *  ------------------- AppOpen Ads -------------------
     */
    var rcAppOpen: Int
        get() = sharedPreferences.getInt(appOpen, 0)
        set(value) = sharedPreferences.edit { putInt(appOpen, value) }

    var rcAppOpenSplash: Int
        get() = sharedPreferences.getInt(appOpenSplash, 1)
        set(value) = sharedPreferences.edit { putInt(appOpenSplash, value) }

    /**
     *  ------------------- Banner Ads -------------------
     */
    var rcBannerHome: Int
        get() = sharedPreferences.getInt(bannerHome, 0)
        set(value) = sharedPreferences.edit { putInt(bannerHome, value) }

    /**
     *  ------------------- Interstitial Ads -------------------
     */
    var rcInterEntrance: Int
        get() = sharedPreferences.getInt(interEntrance, 1)
        set(value) = sharedPreferences.edit { putInt(interEntrance, value) }

    var rcInterOnBoarding: Int
        get() = sharedPreferences.getInt(interOnBoarding, 0)
        set(value) = sharedPreferences.edit { putInt(interOnBoarding, value) }

    /**
     *  ------------------- Native Ads -------------------
     */
    var rcNativeLanguage: Int
        get() = sharedPreferences.getInt(nativeLanguage, 1)
        set(value) = sharedPreferences.edit { putInt(nativeLanguage, value) }

    var rcNativeOnBoarding: Int
        get() = sharedPreferences.getInt(nativeOnBoarding, 0)
        set(value) = sharedPreferences.edit { putInt(nativeOnBoarding, value) }

    var rcNativeHome: Int
        get() = sharedPreferences.getInt(nativeHome, 0)
        set(value) = sharedPreferences.edit { putInt(nativeHome, value) }

    var rcNativeFeature: Int
        get() = sharedPreferences.getInt(nativeFeature, 0)
        set(value) = sharedPreferences.edit { putInt(nativeFeature, value) }

    var rcNativeExit: Int
        get() = sharedPreferences.getInt(nativeExit, 0)
        set(value) = sharedPreferences.edit { putInt(nativeExit, value) }

    /**
     *  ------------------- Rewarded Ads -------------------
     */
    var rcRewardedAiFeature: Int
        get() = sharedPreferences.getInt(rewardedAiFeature, 1)
        set(value) = sharedPreferences.edit { putInt(rewardedAiFeature, value) }

    var rcRewardedInterAiFeature: Int
        get() = sharedPreferences.getInt(rewardedInterAiFeature, 0)
        set(value) = sharedPreferences.edit { putInt(rewardedInterAiFeature, value) }
}