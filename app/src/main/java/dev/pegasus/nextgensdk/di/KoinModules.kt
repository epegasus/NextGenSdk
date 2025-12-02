package dev.pegasus.nextgensdk.di

import com.hypersoft.admobpreloader.interstitialAds.di.interstitialAdsModule
import com.hypersoft.admobpreloader.nativeAds.di.nativeAdsModule
import com.hypersoft.core.di.coreModules

val appModules = listOf(
    coreModules,
    interstitialAdsModule,
    nativeAdsModule
)