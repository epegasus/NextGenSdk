package dev.pegasus.nextgensdk.ui.fragments

import dev.pegasus.nextgensdk.databinding.FragmentDashboardBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class DashboardFragment : BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.mbContinue.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAds() {
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.DASHBOARD)
    }

    private fun checkInterstitialAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.DASHBOARD, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        //findNavController().navigate(R.id.dashboardFragment)
    }
}