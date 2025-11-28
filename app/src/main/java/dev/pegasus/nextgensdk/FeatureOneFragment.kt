package dev.pegasus.nextgensdk

import androidx.navigation.fragment.findNavController
import dev.pegasus.nextgensdk.databinding.FragmentFeatureOneBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class FeatureOneFragment : BaseFragment<FragmentFeatureOneBinding>(FragmentFeatureOneBinding::inflate) {

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