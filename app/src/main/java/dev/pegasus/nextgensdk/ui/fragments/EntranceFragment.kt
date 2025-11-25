package dev.pegasus.nextgensdk.ui.fragments

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.databinding.FragmentEntranceBinding
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnLoadCallBack
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.utils.base.fragment.BaseFragment
import dev.pegasus.nextgensdk.utils.constants.Constants

class EntranceFragment : BaseFragment<FragmentEntranceBinding>(FragmentEntranceBinding::inflate) {

    override fun onViewCreated() {
        startTimeout()
        loadAd()

        binding.mbShowAd.setOnClickListener { showAd() }
    }

    private fun startTimeout() {
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(Constants.TAG, "startTimeout: Starting timeout")
            showAd()
        }, 2000)
    }

    private fun loadAd() {
        diComponent.interstitialAdsConfig.loadInterstitialAd(adType = InterAdKey.ENTRANCE, listener = object : InterstitialOnLoadCallBack {
            override fun onResponse(successfullyLoaded: Boolean) {
                onAdResponse(successfullyLoaded)
            }
        })
    }


    private fun onAdResponse(successfullyLoaded: Boolean) {
        if (isAdded.not()) return
        binding.cpiProgress.visibility = View.GONE
        binding.mbShowAd.isEnabled = true
        binding.mtvTitle.text = "Ad loaded: $successfullyLoaded"
    }

    fun showAd() {
        diComponent.interstitialAdsConfig.showInterstitialAd(activity = activity, adType = InterAdKey.ENTRANCE, listener = object : InterstitialOnShowCallBack {
            override fun onAdFailedToShow() = navigateScreen()
            override fun onAdImpressionDelayed() = navigateScreen()
        })
    }

    private fun navigateScreen() {
        findNavController().navigate(R.id.action_entranceFragment_to_languageFragment)
    }
}