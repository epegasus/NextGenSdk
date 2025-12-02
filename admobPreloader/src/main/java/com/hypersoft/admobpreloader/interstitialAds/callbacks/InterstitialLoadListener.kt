package com.hypersoft.admobpreloader.interstitialAds.callbacks

interface InterstitialLoadListener {
    fun onLoaded(key: String) {}
    fun onFailed(key: String, message: String) {}
}
