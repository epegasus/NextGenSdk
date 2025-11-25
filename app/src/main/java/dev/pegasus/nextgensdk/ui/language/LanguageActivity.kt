package dev.pegasus.nextgensdk.ui.language

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dev.pegasus.nextgensdk.databinding.ActivityLanguageBinding
import dev.pegasus.nextgensdk.di.DIComponent
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.utils.Constants.TAG_ADS

class LanguageActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLanguageBinding.inflate(layoutInflater) }
    private val diComponent by lazy { DIComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadAd()

        binding.mbContinue.setOnClickListener { showAd() }
    }

    private fun loadAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.LANGUAGE, bufferSize = null)
    }

    private fun showAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity = this, adType = InterAdKey.LANGUAGE, listener = object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() {
                Log.e(TAG_ADS, "LanguageActivity -> showAd: onAdFailedToShow")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG_ADS, "LanguageActivity -> showAd: onAdDismissedFullScreenContent")
            }
        })
    }
}