package dev.pegasus.nextgensdk.di

import dev.pegasus.nextgensdk.inter.InterstitialAdsConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DIComponent : KoinComponent {

    val interstitialAdsConfig by inject<InterstitialAdsConfig>()

}