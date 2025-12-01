package dev.pegasus.nextgensdk.ads.inter

interface LoadCallback {
    fun onLoaded()
    fun onFailed(reason: String)
}