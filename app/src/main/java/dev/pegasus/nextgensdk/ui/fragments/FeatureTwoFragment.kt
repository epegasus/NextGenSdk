package dev.pegasus.nextgensdk.ui.fragments

import androidx.navigation.fragment.findNavController
import com.hypersoft.admobpreloader.bannerAds.enums.BannerAdKey
import com.hypersoft.admobpreloader.interstitialAds.callbacks.InterstitialShowListener
import com.hypersoft.admobpreloader.interstitialAds.enums.InterAdKey
import com.hypersoft.admobpreloader.utils.addCleanView
import dev.pegasus.nextgensdk.databinding.FragmentFeatureTwoBinding
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class FeatureTwoFragment : BaseFragment<FragmentFeatureTwoBinding>(FragmentFeatureTwoBinding::inflate) {

    override fun onViewCreated() {
        loadAd()
        loadBanners()

        binding.mbBack.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAd() {
        diComponent.interstitialAdsManager.loadInterstitialAd(InterAdKey.BACK_PRESS)
    }

    private fun loadBanners() {
        diComponent.bannerAdsManager.loadBannerAd(BannerAdKey.DASHBOARD) {
            showTopBanner()
        }
        diComponent.bannerAdsManager.loadBannerAd(BannerAdKey.FEATURE_TWO) {
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
        diComponent.bannerAdsManager.pollBannerAd(BannerAdKey.DASHBOARD)?.let {
            binding.flBannerTop.addCleanView(it.getView(act))
        }
    }

    private fun showBottomBanner() {
        if (isAdded.not()) return
        val act = activity ?: return
        diComponent.bannerAdsManager.pollBannerAd(BannerAdKey.FEATURE_TWO)?.let {
            binding.flBannerBottom.addCleanView(it.getView(act))
        }
    }
}