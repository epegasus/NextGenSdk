package dev.pegasus.nextgensdk.ads.inter.callbacks

interface InterstitialLoadListener {
    fun onLoaded(key: String) {}
    fun onFailed(key: String, message: String) {}
}
