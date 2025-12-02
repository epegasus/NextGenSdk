package dev.pegasus.nextgensdk.ui.fragments

import androidx.navigation.fragment.findNavController
import com.hypersoft.admobpreloader.interstitialAds.callbacks.InterstitialShowListener
import com.hypersoft.admobpreloader.interstitialAds.enums.InterAdKey
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.ads.nativeAds.enums.NativeAdKey
import dev.pegasus.nextgensdk.databinding.FragmentLanguageBinding
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.mbContinue.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAds() {
        loadNative()
    }

    private fun loadNative() {
        diComponent.nativeAdsConfig.loadNativeAd(NativeAdKey.LANGUAGE) { showNativeAd() }
    }

    private fun showNativeAd() {
        diComponent.nativeAdsConfig.pollNativeAd(key = NativeAdKey.LANGUAGE, showCallback = null)?.let {
            if (isAdded.not()) return
            binding.nativeAdView.setNativeAd(it)
        }
    }

    private fun checkInterstitialAd() {
        diComponent.interstitialAdsManager.showInterstitialAd(activity, InterAdKey.ENTRANCE, object : InterstitialShowListener {
            override fun onAdFailedToShow(key: String, reason: String) = navigateScreen()
            override fun onAdImpressionDelayed(key: String) = navigateScreen()
        })
    }

    private fun navigateScreen() {
        findNavController().navigate(R.id.action_languageFragment_to_onBoardingFragment)
    }
}