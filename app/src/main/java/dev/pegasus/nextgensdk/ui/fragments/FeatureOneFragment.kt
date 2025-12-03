package dev.pegasus.nextgensdk.ui.fragments

import androidx.navigation.fragment.findNavController
import com.hypersoft.admobpreloader.bannerAds.enums.BannerAdKey
import com.hypersoft.admobpreloader.interstitialAds.callbacks.InterstitialShowListener
import com.hypersoft.admobpreloader.interstitialAds.enums.InterAdKey
import com.hypersoft.admobpreloader.utils.addCleanView
import dev.pegasus.nextgensdk.databinding.FragmentFeatureOneBinding
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class FeatureOneFragment : BaseFragment<FragmentFeatureOneBinding>(FragmentFeatureOneBinding::inflate) {

    override fun onViewCreated() {
        loadAd()
        loadBanners()

        binding.mbBack.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAd() {
        diComponent.interstitialAdsManager.loadInterstitialAd(InterAdKey.BACK_PRESS)
    }

    private fun loadBanners() {
        // Top collapsible banner (FEATURE_ONE_A)
        diComponent.bannerAdsManager.loadBannerAd(BannerAdKey.FEATURE_ONE_A) {
            showTopBanner()
        }

        // Bottom collapsible banner (FEATURE_ONE_B)
        diComponent.bannerAdsManager.loadBannerAd(BannerAdKey.FEATURE_ONE_B) {
            showBottomBanner()
        }
    }

    private fun checkInterstitialAd() {
        diComponent.interstitialAdsManager.showInterstitialAd(activity, InterAdKey.BACK_PRESS, object : InterstitialShowListener {
            override fun onAdFailedToShow(key: String, reason: String) = navigateBack()
            override fun onAdImpressionDelayed(key: String) = navigateBack()
        })
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }

    private fun showTopBanner() {
        if (isAdded.not()) return
        val act = activity ?: return
        diComponent.bannerAdsManager.pollBannerAd(BannerAdKey.FEATURE_ONE_A)?.let {
            binding.flBannerTop.addCleanView(it.getView(act))
        }
    }

    private fun showBottomBanner() {
        if (isAdded.not()) return
        val act = activity ?: return
        diComponent.bannerAdsManager.pollBannerAd(BannerAdKey.FEATURE_ONE_B)?.let {
            binding.flBannerBottom.addCleanView(it.getView(act))
        }
    }
}