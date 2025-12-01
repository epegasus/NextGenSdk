package dev.pegasus.nextgensdk.ui.fragments

import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentDashboardBinding
import dev.pegasus.nextgensdk.databinding.NativeAdViewBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.nativeads.callbacks.NativeOnShowCallback
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