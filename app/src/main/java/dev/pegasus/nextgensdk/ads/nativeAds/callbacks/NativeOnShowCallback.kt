package dev.pegasus.nextgensdk.ads.nativeAds.callbacks

interface NativeOnShowCallback {
    fun onAdImpression() {}
    fun onAdImpressionDelayed() {}
    fun onAdClicked() {}
    fun onAdFailedToShow() {}
}