package dev.pegasus.nextgensdk.ui.entrance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.pegasus.nextgensdk.databinding.ActivityEntranceBinding
import dev.pegasus.nextgensdk.di.DIComponent
import dev.pegasus.nextgensdk.inter.InterAdKey

class EntranceActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEntranceBinding.inflate(layoutInflater) }
    private val diComponent by lazy { DIComponent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadAds()
    }

    private fun loadAds() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.ENTRANCE)
    }
}