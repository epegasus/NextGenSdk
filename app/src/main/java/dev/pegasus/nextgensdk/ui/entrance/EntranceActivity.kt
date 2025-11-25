package dev.pegasus.nextgensdk.ui.entrance

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.pegasus.nextgensdk.databinding.ActivityEntranceBinding
import dev.pegasus.nextgensdk.di.DIComponent
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnLoadCallBack
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.ui.language.LanguageActivity
import dev.pegasus.nextgensdk.utils.Constants.TAG_ADS

class EntranceActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEntranceBinding.inflate(layoutInflater) }
    private val diComponent by lazy { DIComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadAd()
        startTimeout()

        binding.mbShowAd.setOnClickListener { showAd() }
    }

    private fun loadAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(adType = InterAdKey.ENTRANCE, bufferSize = null, listener = object : InterstitialOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean) {
                binding.cpiProgress.visibility = View.GONE
                binding.mbShowAd.isEnabled = true
                binding.mtvTitle.text = "Ad loaded: $successfullyLoaded"
            }
        })
    }

    private fun startTimeout() {
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG_ADS, "startTimeout: Starting timeout")
            showAd()
        }, 2000)
    }

    fun showAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity = this, adType = InterAdKey.ENTRANCE, listener = object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdDismissedFullScreenContent() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        startActivity(Intent(this, LanguageActivity::class.java))
    }
}