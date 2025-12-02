package dev.pegasus.nextgensdk.ui.fragments

import androidx.navigation.fragment.findNavController
import dev.pegasus.nextgensdk.ads.inter.callbacks.InterstitialShowListener
import dev.pegasus.nextgensdk.ads.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.databinding.FragmentFeatureOneBinding
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class FeatureOneFragment : BaseFragment<FragmentFeatureOneBinding>(FragmentFeatureOneBinding::inflate) {

    override fun onViewCreated() {
        loadAd()

        binding.mbBack.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAd() {
        diComponent.interstitialAdsManager.loadInterstitialAd(InterAdKey.BACK_PRESS)
        //diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.BACK_PRESS)
    }

    private fun checkInterstitialAd() {
        diComponent.interstitialAdsManager.showInterstitialAd(activity, InterAdKey.BACK_PRESS, object : InterstitialShowListener {
            override fun onAdFailedToShow(key: String, reason: String) = navigateBack()
            override fun onAdImpressionDelayed(key: String) = navigateBack()
        })
        /*diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.BACK_PRESS, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateBack()
            override fun onAdImpressionDelayed() = navigateBack()
        })*/
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }
}