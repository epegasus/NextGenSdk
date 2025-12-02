package dev.pegasus.nextgensdk.ads.inter.callbacks

interface InterstitialShowListener {
    fun onAdShown(key: String) {}
    fun onAdImpression(key: String) {}
    fun onAdClicked(key: String) {}
    fun onAdDismissed(key: String) {}
    fun onAdImpressionDelay(key: String) {}
    fun onAdFailedToShow(key: String, reason: String) {}
}