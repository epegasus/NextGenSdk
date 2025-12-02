package com.hypersoft.admobpreloader.interstitialAds.model

data class AdInfo(
    val adUnitId: String,
    val canShare: Boolean,
    val canReuse: Boolean,
    val bufferSize: Int?
)