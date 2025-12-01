package dev.pegasus.nextgensdk.ui.fragments

import androidx.navigation.fragment.findNavController
import dev.pegasus.nextgensdk.databinding.FragmentFeatureTwoBinding
import dev.pegasus.nextgensdk.ads.interstitialAds.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.ads.interstitialAds.enums.InterAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class FeatureTwoFragment : BaseFragment<FragmentFeatureTwoBinding>(FragmentFeatureTwoBinding::inflate) {

    override fun onViewCreated() {
        loadAd()

        binding.mbBack.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.BACK_PRESS)
    }

    private fun checkInterstitialAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.BACK_PRESS, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateBack()
            override fun onAdImpressionDelayed() = navigateBack()
        })
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }
}