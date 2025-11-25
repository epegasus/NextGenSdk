package dev.pegasus.nextgensdk.ui.fragments

import androidx.navigation.fragment.findNavController
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentOnBoardingBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class OnBoardingFragment : BaseFragment<FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.mbContinue.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAds() {
        //loadBanner()      Will do work later
        //loadNative()      Will do work later
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.ON_BOARDING)
    }

    private fun checkInterstitialAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.ON_BOARDING, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        findNavController().navigate(R.id.action_onBoardingFragment_to_dashboardFragment)
    }
}