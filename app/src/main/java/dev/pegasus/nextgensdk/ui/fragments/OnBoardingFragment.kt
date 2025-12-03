package dev.pegasus.nextgensdk.ui.fragments

import androidx.navigation.fragment.findNavController
import com.hypersoft.admobpreloader.bannerAds.enums.BannerAdKey
import com.hypersoft.admobpreloader.interstitialAds.callbacks.InterstitialShowListener
import com.hypersoft.admobpreloader.interstitialAds.enums.InterAdKey
import com.hypersoft.admobpreloader.nativeAds.enums.NativeAdKey
import com.hypersoft.admobpreloader.utils.addCleanView
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentOnBoardingBinding
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class OnBoardingFragment : BaseFragment<FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.mbContinue.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAds() {
        loadBanner()
        loadNative()
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        diComponent.interstitialAdsManager.loadInterstitialAd(InterAdKey.ON_BOARDING)
    }

    private fun loadNative() {
        diComponent.nativeAdsManager.loadNativeAd(NativeAdKey.ON_BOARDING) { showNativeAd() }
    }

    private fun loadBanner() {
        diComponent.bannerAdsManager.loadBannerAd(BannerAdKey.ON_BOARDING) { showBanner() }
    }

    private fun showNativeAd() {
        diComponent.nativeAdsManager.pollNativeAd(key = NativeAdKey.ON_BOARDING, showCallback = null)?.let {
            if (isAdded.not()) return
            binding.nativeAdView.setNativeAd(it)
        }
    }

    private fun showBanner() {
        val act = activity ?: return
        if (isAdded.not()) return
        diComponent.bannerAdsManager.pollBannerAd(key = BannerAdKey.ON_BOARDING)?.let {
            binding.flBanner.addCleanView(it.getView(act))
        }
    }

    private fun checkInterstitialAd() {
        diComponent.interstitialAdsManager.showInterstitialAd(activity, InterAdKey.ON_BOARDING, object : InterstitialShowListener {
            override fun onAdFailedToShow(key: String, reason: String) = navigateScreen()
            override fun onAdImpressionDelayed(key: String) = navigateScreen()
        })
    }

    private fun navigateScreen() {
        findNavController().navigate(R.id.action_onBoardingFragment_to_dashboardFragment)
    }

    override fun onDestroyView() {
        diComponent.bannerAdsManager.clearBannerAd(BannerAdKey.ON_BOARDING)
        super.onDestroyView()
    }
}