package dev.pegasus.nextgensdk.ui.fragments

import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentOnBoardingBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.nativeads.callbacks.NativeOnShowCallback
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
        // Load native on demand for OnBoarding
        diComponent.nativeAdsConfig.loadNativeAd(NativeAdKey.ON_BOARDING)

        val nativeAd = diComponent.nativeAdsConfig.pollNativeAd(
            key = NativeAdKey.ON_BOARDING,
            showCallback = object : NativeOnShowCallback {
                override fun onAdImpression() {
                    // No-op for now
                }

                override fun onAdFailedToShow() {
                    // No-op for now
                }
            }
        ) ?: return

        bindNativeAdToContainer(nativeAd, binding.flNative)
    }

    private fun bindNativeAdToContainer(nativeAd: NativeAd, container: FrameLayout) {
        container.removeAllViews()

        val inflater: LayoutInflater = layoutInflater
        val adView = inflater.inflate(R.layout.native_ad_view, container, false) as NativeAdView

        val headlineView: TextView = adView.findViewById(R.id.adHeadline)
        val bodyView: TextView = adView.findViewById(R.id.adBody)
        val iconView: ImageView = adView.findViewById(R.id.adAppIcon)
        val ctaView: TextView = adView.findViewById(R.id.adCallToAction)

        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.iconView = iconView
        adView.callToActionView = ctaView

        headlineView.text = nativeAd.headline
        bodyView.text = nativeAd.body
        ctaView.text = nativeAd.callToAction
        iconView.setImageDrawable(nativeAd.icon?.drawable)

        bodyView.visibility = if (nativeAd.body == null) android.view.View.GONE else android.view.View.VISIBLE
        iconView.visibility = if (nativeAd.icon == null) android.view.View.GONE else android.view.View.VISIBLE

        container.addView(adView)
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