package dev.pegasus.nextgensdk.ads.inter

interface ShowCallback {
    fun onShown()
    fun onDismissed()
    fun onFailed(reason: String)
}