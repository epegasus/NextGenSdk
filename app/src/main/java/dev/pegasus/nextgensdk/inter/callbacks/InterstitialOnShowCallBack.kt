package dev.pegasus.nextgensdk.inter.callbacks

interface InterstitialOnShowCallBack {
    fun onAdDismissedFullScreenContent() {}
    fun onAdFailedToShow()
    fun onAdShowedFullScreenContent() {}
    fun onAdImpression() {}
    fun onAdClicked() {}
    fun onAdImpressionDelayed() {}
}