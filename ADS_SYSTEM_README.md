# Ad System Documentation

## Overview

This ad system is designed for Android apps using Google's Mobile Ads Next-Gen SDK (GMA Next-Gen). It provides a clean, modular, and performance-friendly architecture with automatic lifecycle management, preloading where recommended, and simple one-line API calls from your UI.

## Architecture

### Core Components

1. **AdConfig Interface** - Base interface for all ad configurations
2. **BannerAdsConfig** - Handles banner ads (regular loading)
3. **NativeAdsConfig** - Handles native ads (preloading API)
4. **InterstitialAdsConfig** - Handles interstitial ads (preloading API with buffering)
5. **AppOpenAdsConfig** - Handles app open ads (preloading API)
6. **InterstitialManager** - Coordinates interstitial showing across screens

### Key Features

- ✅ **Preloading where recommended**: Interstitials, Native, and App Open ads use preloading API
- ✅ **Lifecycle-aware**: Automatic cleanup on activity destruction
- ✅ **Memory-efficient**: Configurable buffer sizes, automatic ad destruction
- ✅ **DI-friendly**: Koin integration for easy testing and modularity
- ✅ **Simple API**: One-line calls (`loadAd()`, `showAd()`) from UI

## Setup

### 1. Dependencies

The system uses:
- Google Mobile Ads Next-Gen SDK (`com.google.android.libraries.ads.mobile.sdk:ads-mobile-sdk`)
- Koin for dependency injection (`io.insert-koin:koin-android`)
- AndroidX Lifecycle (`androidx.lifecycle:lifecycle-runtime-ktx`)

### 2. Ad Unit IDs

Update `AdUnitIds.kt` with your actual AdMob ad unit IDs:

```kotlin
object AdUnitIds {
    const val MAIN_BANNER = "your-banner-ad-unit-id"
    const val LANGUAGE_NATIVE = "your-native-ad-unit-id"
    // ... etc
}
```

### 3. Application Class

The `NextGenApplication` class initializes Koin. Make sure it's registered in `AndroidManifest.xml`:

```xml
<application android:name=".NextGenApplication" ...>
```

## Usage

### MainActivity

**Requirements**: Banner at bottom, interstitial after 10s (or earlier if loaded), then navigate.

```kotlin
// Banner ad
bannerConfig = BannerAdsConfig(AdUnitIds.MAIN_BANNER, binding.flBanner, AdSize.BANNER)
bannerConfig.attachLifecycle(this)
bannerConfig.loadAd() // One-line call

// Interstitial ad
interstitialConfig = InterstitialAdsConfig(AdUnitIds.MAIN_INTERSTITIAL, this, bufferSize = 1)
interstitialConfig.attachLifecycle(this)
interstitialConfig.setOnAdDismissedCallback { navigateToLanguage() }
interstitialConfig.loadAd() // Preloads in background
```

### LanguageActivity

**Requirements**: Native ad at bottom, interstitial on Continue (if not shown in MainActivity).

```kotlin
// Native ad
nativeConfig = NativeAdsConfig(AdUnitIds.LANGUAGE_NATIVE, binding.flNative, nativeAdView, this)
nativeConfig.attachLifecycle(this)
nativeConfig.loadAd() // Preloads
nativeConfig.showAd() // Shows if ready

// Interstitial (only if not shown in MainActivity)
if (!interstitialManager.wasShownInMainActivity()) {
    interstitialConfig.showAd()
}
```

### OnBoardingActivity

**Requirements**: Banner top + Native bottom, interstitial with different ID on Continue.

```kotlin
// Banner at top
bannerConfig = BannerAdsConfig(AdUnitIds.ONBOARDING_BANNER, binding.flBanner, AdSize.BANNER)
bannerConfig.attachLifecycle(this)
bannerConfig.loadAd()

// Native at bottom
nativeConfig = NativeAdsConfig(AdUnitIds.ONBOARDING_NATIVE, binding.flNative, nativeAdView, this)
nativeConfig.attachLifecycle(this)
nativeConfig.loadAd()
nativeConfig.showAd()

// Interstitial with different ID
interstitialConfig = InterstitialAdsConfig(AdUnitIds.ONBOARDING_INTERSTITIAL, this, bufferSize = 1)
interstitialConfig.attachLifecycle(this)
interstitialConfig.loadAd()
```

### DashboardActivity

**Requirements**: Native bottom, interstitial on "feature one" button.

```kotlin
// Native ad
nativeConfig = NativeAdsConfig(AdUnitIds.DASHBOARD_NATIVE, binding.flNative, nativeAdView, this)
nativeConfig.attachLifecycle(this)
nativeConfig.loadAd()
nativeConfig.showAd()

// Interstitial on button click
interstitialConfig = InterstitialAdsConfig(AdUnitIds.DASHBOARD_INTERSTITIAL, this, bufferSize = 2)
interstitialConfig.attachLifecycle(this)
interstitialConfig.loadAd()

// On button click:
interstitialConfig.showAd() // One-line call
```

## Preloading Strategy

### Where Preloading is Used

1. **Interstitial Ads** - Preloaded with configurable buffer (default: 2)
   - Perfect for user actions (button clicks, screen transitions)
   - Buffer size balances show rate vs memory usage

2. **Native Ads** - Preloaded (buffer: 1)
   - Heavier ads that benefit from background loading
   - Automatically refills buffer after showing

3. **App Open Ads** - Preloaded (buffer: 1)
   - Must be ready instantly at app launch

### Where Regular Loading is Used

1. **Banner Ads** - Regular loading
   - Lightweight, load quickly
   - No need for preloading overhead

## Memory Management

- **Automatic cleanup**: All ads are destroyed on activity `onDestroy()`
- **Configurable buffers**: Adjust buffer sizes based on your needs
- **Ad destruction**: Preloaded ads are properly destroyed when not needed
- **Lifecycle observers**: Automatic cleanup prevents memory leaks

## Testing

The system is designed to be testable:

1. **Koin DI**: Easy to mock ad configs in tests
2. **Interface-based**: `AdConfig` interface allows for test doubles
3. **No static dependencies**: All dependencies injected via Koin

## Customization

### Adjust Buffer Sizes

```kotlin
// For interstitials
interstitialConfig = InterstitialAdsConfig(adUnitId, activity, bufferSize = 3)

// For native ads
nativeConfig.setBufferSize(2)
```

### Add Callbacks

```kotlin
interstitialConfig.setOnAdDismissedCallback {
    // Handle ad dismissal
}

interstitialConfig.setOnAdFailedCallback { error ->
    // Handle ad load failure
}
```

## Notes

### GMA Next-Gen SDK API

The implementation uses the GMA Next-Gen SDK preloading APIs. If you encounter API differences:

1. **Preload API**: The `preload()` method may have different signatures. Check the SDK version.
2. **NativeAdView**: The `setNativeAd()` method should automatically map views if IDs match expected asset types.
3. **NativeAdHelper**: Provides explicit mapping as a fallback.

### Troubleshooting

- **Ads not showing**: Check ad unit IDs, network connectivity, and SDK initialization
- **Memory issues**: Reduce buffer sizes or check for proper cleanup
- **Preload not working**: Verify SDK version supports preloading API

## File Structure

```
app/src/main/java/dev/pegasus/nextgensdk/
├── ads/
│   ├── AdConfig.kt              # Base interface
│   ├── AdUnitIds.kt             # Ad unit ID constants
│   ├── BannerAdsConfig.kt       # Banner ad config
│   ├── NativeAdsConfig.kt      # Native ad config
│   ├── InterstitialAdsConfig.kt # Interstitial ad config
│   ├── AppOpenAdsConfig.kt     # App open ad config
│   ├── InterstitialManager.kt   # Shared interstitial coordinator
│   └── NativeAdHelper.kt       # Native ad view mapping helper
├── di/
│   └── AdModule.kt              # Koin DI module
├── NextGenApplication.kt       # Application class with Koin init
└── screens/
    ├── MainActivity.kt          # Main screen with banner + interstitial
    ├── LanguageActivity.kt      # Language screen with native + interstitial
    ├── OnBoardingActivity.kt    # Onboarding with banner + native + interstitial
    └── DashboardActivity.kt    # Dashboard with native + interstitial
```

## Best Practices

1. **Always call `attachLifecycle()`** - Ensures proper cleanup
2. **Preload early** - Call `loadAd()` as soon as possible
3. **Check `isReady()`** - Before showing, verify ad is ready
4. **Handle failures gracefully** - Show fallback UI if ads fail
5. **Monitor buffer sizes** - Balance show rate vs memory usage
6. **Use test ad unit IDs** - During development

## Support

For issues or questions:
1. Check GMA Next-Gen SDK documentation
2. Verify ad unit IDs are correct
3. Check logcat for ad loading errors
4. Ensure SDK is properly initialized

---

**Built with ❤️ for clean, maintainable ad integration**

