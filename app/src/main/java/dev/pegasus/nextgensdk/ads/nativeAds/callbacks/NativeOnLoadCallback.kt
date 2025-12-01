package dev.pegasus.nextgensdk.ads.nativeAds.callbacks

fun interface NativeOnLoadCallback {
    fun onResponse(isLoaded: Boolean)
}