package dev.pegasus.nextgensdk.ui.fragments

import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentLanguageBinding
import dev.pegasus.nextgensdk.databinding.NativeAdViewBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.nativeads.enums.NativeAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.mbContinue.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAds() {
        loadNative()
    }

    private fun loadNative() {
        diComponent.nativeAdsConfig.loadNativeAd(NativeAdKey.LANGUAGE) { showNativeAd() }
    }

    private fun showNativeAd() {
        diComponent.nativeAdsConfig.pollNativeAd(key = NativeAdKey.LANGUAGE, showCallback = null)?.let {
            if (isAdded.not()) return
            bindNativeAdToContainer(it, binding.flNative)
        }
    }

    private fun bindNativeAdToContainer(nativeAd: NativeAd, container: FrameLayout) {
        val nativeAdBinding = NativeAdViewBinding.inflate(layoutInflater)
        container.removeAllViews()
        container.addView(nativeAdBinding.root)

        // Set the native ad view elements.
        val nativeAdView = nativeAdBinding.root
        nativeAdView.advertiserView = nativeAdBinding.adAttribute
        nativeAdView.bodyView = nativeAdBinding.adBody
        nativeAdView.callToActionView = nativeAdBinding.adCallToAction
        nativeAdView.headlineView = nativeAdBinding.adHeadline
        nativeAdView.iconView = nativeAdBinding.adAppIcon

        // Set the view element with the native ad assets.
        nativeAdBinding.adAttribute.text = nativeAd.advertiser
        nativeAdBinding.adBody.text = nativeAd.body
        nativeAdBinding.adCallToAction.text = nativeAd.callToAction
        nativeAdBinding.adHeadline.text = nativeAd.headline
        nativeAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)

        // Hide views for assets that don't have data.
        nativeAdBinding.adAppIcon.isVisible = nativeAd.icon != null

        // Inform the Google Mobile Ads SDK that you have finished populating the native ad views with this native ad.
        nativeAdView.registerNativeAd(nativeAd, nativeAdBinding.adMediaView)
    }

    private fun checkInterstitialAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.ENTRANCE, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        findNavController().navigate(R.id.action_languageFragment_to_onBoardingFragment)
    }
}