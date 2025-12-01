package dev.pegasus.nextgensdk.ui.fragments

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentDashboardBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.nativeads.enums.NativeAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class DashboardFragment : BaseFragment<FragmentDashboardBinding>(FragmentDashboardBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.mbFeatureOne.setOnClickListener { checkInterstitialAd(0) }
        binding.mbFeatureTwo.setOnClickListener { checkInterstitialAd(1) }
        binding.mbMenuOne.setOnClickListener { checkInterstitialBottomNavigationAd(0) }
        binding.mbMenuTwo.setOnClickListener { checkInterstitialBottomNavigationAd(1) }
    }

    override fun onResume() {
        super.onResume()
        registerBackPress()
    }

    private fun loadAds() {
        loadInterstitialAd()
        loadNative()
    }

    private fun registerBackPress() {
        (activity as? AppCompatActivity)?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_dashboardFragment_to_exitFragment)
                }
            }
        )
    }

    private fun loadInterstitialAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.DASHBOARD)
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.BOTTOM_NAVIGATION)
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.EXIT)
    }

    private fun loadNative() {
        diComponent.nativeAdsConfig.loadNativeAd(NativeAdKey.DASHBOARD) { showNativeAd() }
    }

    private fun showNativeAd() {
        diComponent.nativeAdsConfig.pollNativeAd(key = NativeAdKey.DASHBOARD, showCallback = null)?.let {
            if (isAdded.not()) return
            binding.nativeAdView.setNativeAd(it)
        }
    }

    private fun checkInterstitialAd(caseType: Int) {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.DASHBOARD, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen(caseType)
            override fun onAdImpressionDelayed() = navigateScreen(caseType)
        })
    }

    private fun checkInterstitialBottomNavigationAd(caseType: Int) {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.BOTTOM_NAVIGATION, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() {}
            override fun onAdImpressionDelayed() {}
        })
    }

    private fun navigateScreen(caseType: Int) {
        when (caseType) {
            0 -> findNavController().navigate(R.id.action_dashboardFragment_to_featureOneFragment)
            1 -> findNavController().navigate(R.id.action_dashboardFragment_to_featureTwoFragment)
        }
    }
}