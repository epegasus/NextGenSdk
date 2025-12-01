package dev.pegasus.nextgensdk.ui.fragments

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentLanguageBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.nativeads.enums.NativeAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment

class LanguageFragment : BaseFragment<FragmentLanguageBinding>(FragmentLanguageBinding::inflate) {

    private var isLanguageNativeBound = false

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
        container.removeAllViews()

        val inflater: LayoutInflater = layoutInflater
        val adView = inflater.inflate(R.layout.native_ad_view, container, false) as NativeAdView

        // Map views
        val headlineView: TextView = adView.findViewById(R.id.adHeadline)
        val bodyView: TextView = adView.findViewById(R.id.adBody)
        val iconView: ImageView = adView.findViewById(R.id.adAppIcon)
        val ctaView: TextView = adView.findViewById(R.id.adCallToAction)
        val mediaView: MediaView = adView.findViewById(R.id.adMedia)

        adView.headlineView = headlineView
        adView.bodyView = bodyView
        adView.iconView = iconView
        adView.callToActionView = ctaView

        // Fill data
        headlineView.text = nativeAd.headline
        bodyView.text = nativeAd.body
        ctaView.text = nativeAd.callToAction
        iconView.setImageDrawable(nativeAd.icon?.drawable)

        // Basic visibility handling
        bodyView.visibility = if (nativeAd.body == null) View.GONE else View.VISIBLE
        iconView.visibility = if (nativeAd.icon == null) View.GONE else View.VISIBLE

        container.addView(adView)

        // Important: register the native ad with its view so impressions are tracked
        adView.registerNativeAd(nativeAd, mediaView)
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