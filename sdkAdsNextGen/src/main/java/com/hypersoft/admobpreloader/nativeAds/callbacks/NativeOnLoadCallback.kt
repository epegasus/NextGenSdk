package com.hypersoft.admobpreloader.nativeAds.callbacks

fun interface NativeOnLoadCallback {
    fun onResponse(isLoaded: Boolean)
}