# Quick Start Guide

## 🚀 Getting Started

### 1. Update Ad Unit IDs

Edit `app/src/main/java/dev/pegasus/nextgensdk/ads/AdUnitIds.kt`:

```kotlin
object AdUnitIds {
    const val MAIN_BANNER = "ca-app-pub-XXXXXXXX/XXXXXXXX" // Your banner ID
    const val LANGUAGE_NATIVE = "ca-app-pub-XXXXXXXX/XXXXXXXX" // Your native ID
    // ... replace all test IDs with your actual AdMob ad unit IDs
}
```

### 2. Verify Application Class

Ensure `NextGenApplication` is registered in `AndroidManifest.xml` (already done ✅).

### 3. Build and Run

The ad system is fully integrated! Each activity automatically:
- Loads ads on creation
- Manages lifecycle automatically
- Shows ads at appropriate times

## 📱 Screen-by-Screen Behavior

### MainActivity
- ✅ Banner ad loads automatically at bottom
- ✅ Interstitial preloads and shows after 10s (or earlier if ready)
- ✅ Navigates to LanguageActivity after interstitial

### LanguageActivity  
- ✅ Native ad loads and shows at bottom
- ✅ Interstitial shows on Continue (only if not shown in MainActivity)
- ✅ Navigates to OnBoardingActivity after interstitial

### OnBoardingActivity
- ✅ Banner ad at top
- ✅ Native ad at bottom  
- ✅ Interstitial with different ID shows on Continue
- ✅ Navigates to DashboardActivity after interstitial

### DashboardActivity
- ✅ Native ad at bottom
- ✅ Interstitial shows on "feature one" button click

## 🎯 Key API Calls

All activities use simple one-line calls:

```kotlin
// Load ad (preloads if supported)
config.loadAd()

// Show ad (if ready)
config.showAd()

// Attach lifecycle (always call this!)
config.attachLifecycle(this)
```

## ⚙️ Customization

### Adjust Buffer Sizes

```kotlin
// More aggressive preloading (higher memory usage)
interstitialConfig = InterstitialAdsConfig(adUnitId, activity, bufferSize = 3)

// Less aggressive (lower memory usage)
interstitialConfig = InterstitialAdsConfig(adUnitId, activity, bufferSize = 1)
```

### Add Callbacks

```kotlin
interstitialConfig.setOnAdDismissedCallback {
    // Custom logic after ad dismissal
}

interstitialConfig.setOnAdFailedCallback { error ->
    // Handle ad load failure
    Log.e("Ads", "Failed: $error")
}
```

## 🐛 Troubleshooting

### Ads Not Showing?
1. ✅ Check ad unit IDs are correct (not test IDs in production)
2. ✅ Verify internet connection
3. ✅ Check logcat for errors (filter by "AdsConfig" or "MainActivity")
4. ✅ Ensure AdMob SDK initialized successfully

### Memory Issues?
1. ✅ Reduce buffer sizes (default: 2 for interstitials, 1 for native)
2. ✅ Check that `attachLifecycle()` is called
3. ✅ Verify ads are destroyed on activity destroy

### Preload Not Working?
1. ✅ Check GMA Next-Gen SDK version (0.22.0-beta01)
2. ✅ Verify preload API exists in your SDK version
3. ✅ Check logcat for preload errors

## 📚 Documentation

See `ADS_SYSTEM_README.md` for complete documentation.

## ✨ Features

- ✅ Preloading where recommended (Interstitials, Native, App Open)
- ✅ Automatic lifecycle management
- ✅ Memory-efficient buffering
- ✅ Koin DI integration
- ✅ Simple one-line API
- ✅ Test-friendly architecture

---

**Ready to go! 🎉**

