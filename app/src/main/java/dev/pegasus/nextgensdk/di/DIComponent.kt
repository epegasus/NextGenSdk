package dev.pegasus.nextgensdk.di

import dev.pegasus.nextgensdk.ads.interstitialAds.InterstitialAdsConfig
import dev.pegasus.nextgensdk.ads.nativeAds.NativeAdsConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DIComponent : KoinComponent {

    val interstitialAdsConfig by inject<InterstitialAdsConfig>()
    val nativeAdsConfig by inject<NativeAdsConfig>()

}