package dev.pegasus.nextgensdk.ui.fragments

import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentDashboardBinding
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
        // Load native on demand for Dashboard
        diComponent.nativeAdsConfig.loadNativeAd(NativeAdKey.DASHBOARD)

        val nativeAd = diComponent.nativeAdsConfig.pollNativeAd(
            key = NativeAdKey.DASHBOARD,
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
        val mediaView: MediaView = adView.findViewById(R.id.adMedia)

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

        // Register native ad with its view so SDK can track impressions
        adView.registerNativeAd(nativeAd, mediaView)
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