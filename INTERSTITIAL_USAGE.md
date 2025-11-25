# Interstitial Ads Usage Guide

## New Simplified API

All interstitial ads are now managed by a single `InterstitialAdsConfig` class using enum keys.

## Setup

```kotlin
// In your Activity
private lateinit var interstitialConfig: InterstitialAdsConfig

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupAds()
}

private fun setupAds() {
    // Setup interstitial manager (one instance for all ads)
    interstitialConfig = setupInterstitial()
    
    // Load ads for specific keys
    interstitialConfig.loadInterstitialAd(InterAdKey.ENTRANCE)
    interstitialConfig.loadInterstitialAd(InterAdKey.ONBOARDING)
}
```

## Usage Examples

### Example 1: Entrance Interstitial (MainActivity)

```kotlin
// Load the ad
interstitialConfig.loadInterstitialAd(InterAdKey.ENTRANCE)

// Check if loaded
if (interstitialConfig.isInterstitialAdLoaded(InterAdKey.ENTRANCE)) {
    // Ad is ready
}

// Show the ad with callback
interstitialConfig.showInterstitialAd(InterAdKey.ENTRANCE) {
    // This callback is invoked when:
    // - Ad is dismissed, OR
    // - No ad is available
    navigateToNextScreen()
}
```

### Example 2: OnBoarding Interstitial (Button Click)

```kotlin
binding.btnContinue.setOnClickListener {
    interstitialConfig.showInterstitialAd(InterAdKey.ONBOARDING) {
        // Navigate after ad dismissed or if no ad available
        navigateToDashboard()
    }
}
```

### Example 3: Dashboard Interstitial (Feature Button)

```kotlin
binding.btnFeatureOne.setOnClickListener {
    interstitialConfig.showInterstitialAd(InterAdKey.DASHBOARD) {
        // Handle after ad dismissed or if no ad available
        // (Optional - can be empty if no action needed)
    }
}
```

## Available Keys

```kotlin
enum class InterAdKey(val adUnitId: String) {
    ENTRANCE(AdUnitIds.MAIN_INTERSTITIAL),      // MainActivity entrance
    LANGUAGE(AdUnitIds.LANGUAGE_INTERSTITIAL),   // Language screen
    ONBOARDING(AdUnitIds.ONBOARDING_INTERSTITIAL), // Onboarding screen
    DASHBOARD(AdUnitIds.DASHBOARD_INTERSTITIAL)   // Dashboard screen
}
```

## API Methods

### `loadInterstitialAd(key: InterAdKey)`
Loads/preloads an interstitial ad for the specified key.

```kotlin
interstitialConfig.loadInterstitialAd(InterAdKey.ENTRANCE)
```

### `showInterstitialAd(key: InterAdKey, interDismissCallback: () -> Unit)`
Shows the interstitial ad for the specified key. Callback is invoked when:
- Ad is dismissed by user, OR
- No ad is available to show

```kotlin
interstitialConfig.showInterstitialAd(InterAdKey.ONBOARDING) {
    navigateToNextScreen()
}
```

### `isInterstitialAdLoaded(key: InterAdKey): Boolean`
Checks if an interstitial ad is loaded and ready to show.

```kotlin
if (interstitialConfig.isInterstitialAdLoaded(InterAdKey.ENTRANCE)) {
    // Ad is ready
}
```

## Benefits

✅ **Single class** - One `InterstitialAdsConfig` manages all interstitials  
✅ **Type-safe keys** - Enum prevents typos and wrong ad IDs  
✅ **Simple API** - Just 3 methods: load, show, isLoaded  
✅ **Automatic callback** - Handles both ad dismissal and no-ad scenarios  
✅ **Preloading** - Uses GMA Next-Gen preloading API for better performance  

## Complete Example

```kotlin
class MyActivity : AppCompatActivity() {
    
    private lateinit var interstitialConfig: InterstitialAdsConfig
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupAds()
    }
    
    private fun setupAds() {
        // Setup once
        interstitialConfig = setupInterstitial()
        
        // Load ads you'll need
        interstitialConfig.loadInterstitialAd(InterAdKey.ENTRANCE)
        interstitialConfig.loadInterstitialAd(InterAdKey.ONBOARDING)
    }
    
    private fun onButtonClick() {
        // Show ad - callback handles navigation
        interstitialConfig.showInterstitialAd(InterAdKey.ONBOARDING) {
            navigateToNextScreen()
        }
    }
    
    private fun checkIfReady() {
        if (interstitialConfig.isInterstitialAdLoaded(InterAdKey.ENTRANCE)) {
            // Ad is ready to show
        }
    }
}
```

