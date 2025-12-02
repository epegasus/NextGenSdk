package dev.pegasus.nextgensdk.di

import com.hypersoft.admobpreloader.interstitialAds.InterstitialAdsManager
import com.hypersoft.admobpreloader.nativeAds.NativeAdsManager
import com.hypersoft.admobpreloader.bannerAds.BannerAdsManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DIComponent : KoinComponent {

    val interstitialAdsManager by inject<InterstitialAdsManager>()
    val nativeAdsManager by inject<NativeAdsManager>()
    val bannerAdsManager by inject<BannerAdsManager>()

}