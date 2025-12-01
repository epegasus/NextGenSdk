package dev.pegasus.nextgensdk.nativeads.callbacks

interface NativeOnShowCallback {
    fun onAdImpression() {}
    fun onAdImpressionDelayed() {}
    fun onAdClicked() {}
    fun onAdFailedToShow() {}
}