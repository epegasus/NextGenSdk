## NextGenSdk

NextGenSdk is a multi-module Android project based on a Google Mobile Ads (GMA) **Next Gen Ads SDK**. It showcases a preloaded, event-driven strategy for banners, interstitial, and native ads.

### Modules
- **app**: Sample app demonstrating real usage and navigation flows using the Next Gen SDK.
- **core**: Shared core utilities (DI, networking, storage, shared preferences, etc.).
- **sdkAdsNextGen**: GMA-based ads SDK module (banners, interstitial, and native) with preload + show engines and simple callbacks.

### Tech Stack
- **Language**: Kotlin
- **Architecture**: Multi-module, DI (Koin-based setup via `CoreModules` and feature modules)
- **Build system**: Gradle (Kotlin DSL)

### Preload Strategy (Next Gen SDK)
- **Preloading**: Ads are preloaded in advance using `PreloadEngine` for each ad type to minimize latency at show time.
- **Show flow**: `ShowEngine` reads from the internal `AdRegistry` so that show calls are fast and resilient.
- **Callbacks**: Typed callbacks (`*OnLoadCallback`, `*OnShowCallback`, and listeners) report load/show success or failure.
- **Ad keys/types**: Enum-based keys (e.g. `BannerAdKey`, `InterAdKey`, `NativeAdKey`) make it easy to configure and track placements.

### Getting Started
- **Requirements**: Android Studio (latest), JDK 17+, Android Gradle Plugin compatible with your Studio version.
- **Open the project** in Android Studio using `settings.gradle.kts`.
- **Sync Gradle** and run the `app` module on a device/emulator.

### Basic Integration (SDK Module)
- **Add dependency** on `sdkAdsNextGen` from your app module.
- **Initialize DI** using the provided Koin modules in `core` and `sdkAdsNextGen`.
- **Use managers** like `BannerAdsManager`, `InterstitialAdsManager`, and `NativeAdsManager` to preload and show ads according to your app flow.

### License
This project is currently provided without a specific license. Please ask the author before using it in production.