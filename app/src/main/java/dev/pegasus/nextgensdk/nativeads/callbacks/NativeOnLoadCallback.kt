package dev.pegasus.nextgensdk.nativeads.callbacks

fun interface NativeOnLoadCallback {
    fun onResponse(isLoaded: Boolean)
}