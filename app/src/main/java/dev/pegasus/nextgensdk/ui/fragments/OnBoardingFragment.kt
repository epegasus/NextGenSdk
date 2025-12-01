package dev.pegasus.nextgensdk.ui.fragments

import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentOnBoardingBinding
import dev.pegasus.nextgensdk.databinding.NativeAdViewBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.nativeads.enums.NativeAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class OnBoardingFragment : BaseFragment<FragmentOnBoardingBinding>(FragmentOnBoardingBinding::inflate) {

    override fun onViewCreated() {
        loadAds()

        binding.mbContinue.setOnClickListener { checkInterstitialAd() }
    }

    private fun loadAds() {
        //loadBanner()      Will do work later
        loadNative()
        loadInterstitialAd()
    }

    private fun loadInterstitialAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(InterAdKey.ON_BOARDING)
    }

    private fun loadNative() {
        diComponent.nativeAdsConfig.loadNativeAd(NativeAdKey.ON_BOARDING) { showNativeAd() }
    }

    private fun showNativeAd() {
        diComponent.nativeAdsConfig.pollNativeAd(key = NativeAdKey.ON_BOARDING, showCallback = null)?.let {
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
        diComponent.interstitialAdsConfig.showInterstitialAd(activity, InterAdKey.ON_BOARDING, object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        findNavController().navigate(R.id.action_onBoardingFragment_to_dashboardFragment)
    }
}