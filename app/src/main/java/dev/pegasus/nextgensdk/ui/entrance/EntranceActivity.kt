package dev.pegasus.nextgensdk.ui.entrance

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.pegasus.nextgensdk.databinding.ActivityEntranceBinding
import dev.pegasus.nextgensdk.di.DIComponent
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnLoadCallBack
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.utils.Constants.TAG_ADS

class EntranceActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEntranceBinding.inflate(layoutInflater) }
    private val diComponent by lazy { DIComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadAds()

        binding.mbShowAd.setOnClickListener { showAds() }
    }

    private fun loadAds() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(
            adType = InterAdKey.ENTRANCE,
            listener = object : InterstitialOnLoadCallBack {
                override fun onResponse(successfullyLoaded: Boolean) {
                    this@EntranceActivity.runOnUiThread {
                        binding.cpiProgress.visibility = View.GONE
                        binding.mbShowAd.isEnabled = true
                        binding.mtvTitle.text = "Ad loaded: $successfullyLoaded"
                        if (successfullyLoaded) {
                            Log.d(TAG_ADS, "EntranceActivity -> loadAds: Ad loaded successfully")
                        } else {
                            Log.e(TAG_ADS, "EntranceActivity -> loadAds: Ad failed to load")
                        }
                    }
                }
            }
        )
    }

    fun showAds() {
        if (diComponent.interstitialAdsConfig.isInterstitialAdLoaded(InterAdKey.ENTRANCE)) {
            diComponent.interstitialAdsConfig.showInterstitialAd(
                activity = this,
                adType = InterAdKey.ENTRANCE,
                listener = object : InterstitialOnShowCallBack {
                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        Log.d(TAG_ADS, "EntranceActivity -> showAds: onAdShowedFullScreenContent")
                    }

                    override fun onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent()
                        Log.d(TAG_ADS, "EntranceActivity -> showAds: onAdDismissedFullScreenContent")
                    }

                    override fun onAdFailedToShow() {
                        super.onAdFailedToShow()
                        Log.e(TAG_ADS, "EntranceActivity -> showAds: onAdFailedToShow")
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        Log.d(TAG_ADS, "EntranceActivity -> showAds: onAdImpression")
                    }

                    override fun onAdImpressionDelayed() {
                        super.onAdImpressionDelayed()
                        Log.d(TAG_ADS, "EntranceActivity -> showAds: onAdImpressionDelayed")
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        Log.d(TAG_ADS, "EntranceActivity -> showAds: onAdClicked")
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}