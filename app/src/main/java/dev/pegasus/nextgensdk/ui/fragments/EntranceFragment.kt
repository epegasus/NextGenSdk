package dev.pegasus.nextgensdk.ui.fragments

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import dev.pegasus.nextgensdk.R
import com.hypersoft.admobpreloader.interstitialAds.callbacks.InterstitialLoadListener
import com.hypersoft.admobpreloader.interstitialAds.callbacks.InterstitialShowListener
import com.hypersoft.admobpreloader.interstitialAds.enums.InterAdKey
import com.hypersoft.admobpreloader.nativeAds.enums.NativeAdKey
import dev.pegasus.nextgensdk.databinding.FragmentEntranceBinding
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment
import dev.pegasus.nextgensdk.utils.constants.Constants.TAG

class EntranceFragment : BaseFragment<FragmentEntranceBinding>(FragmentEntranceBinding::inflate) {

    override fun onViewCreated() {
        startTimeout()
        loadAd()

        binding.mbShowAd.setOnClickListener { showAd() }
    }

    private fun startTimeout() {
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "startTimeout: Starting timeout")
            onAdResponse("Ad Timeout")
        }, 2000)
    }

    private fun loadAd() {
        diComponent.interstitialAdsManager.loadInterstitialAd(InterAdKey.ENTRANCE, object : InterstitialLoadListener {
            override fun onLoaded(key: String) = onAdResponse("Ad loaded: $key")
            override fun onFailed(key: String, message: String) = onAdResponse("Ad failed to load: $message")
        })

        // Preload native for Language screen ahead of time
        diComponent.nativeAdsManager.loadNativeAd(NativeAdKey.LANGUAGE)
    }

    private fun onAdResponse(message: String) {
        if (isAdded.not()) return
        binding.cpiProgress.visibility = View.GONE
        binding.mbShowAd.isEnabled = true
        binding.mtvTitle.text = message
    }

    fun showAd() {
        diComponent.interstitialAdsManager.showInterstitialAd(activity, InterAdKey.ENTRANCE, object : InterstitialShowListener {
            override fun onAdFailedToShow(key: String, reason: String) = navigateScreen()
            override fun onAdImpressionDelayed(key: String) = navigateScreen()
        })
    }

    private fun navigateScreen() {
        findNavController().navigate(R.id.action_entranceFragment_to_languageFragment)
    }
}