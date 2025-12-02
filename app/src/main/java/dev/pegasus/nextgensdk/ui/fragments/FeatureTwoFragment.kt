package dev.pegasus.nextgensdk.ui.fragments

import androidx.navigation.fragment.findNavController
import com.hypersoft.admobpreloader.interstitialAds.callbacks.InterstitialShowListener
import com.hypersoft.admobpreloader.interstitialAds.enums.InterAdKey
import dev.pegasus.nextgensdk.databinding.FragmentFeatureTwoBinding
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class FeatureTwoFragment : BaseFragment<FragmentFeatureTwoBinding>(FragmentFeatureTwoBinding::inflate) {

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