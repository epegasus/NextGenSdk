# Interstitial Ads Test Scenarios & Show-Rate Optimization

## Overview
This document outlines test scenarios for the `InterstitialAdsConfig` class, focusing on `reuseAd` logic and show-rate optimization based on marketing team requirements.

## Current Configuration Analysis

### Ad Types and Their Reuse Settings

| Ad Type | reuseAd | bufferSize | Ad Unit ID (Debug) | Purpose |
|---------|---------|------------|-------------------|---------|
| **ENTRANCE** | `true` | `null` | `admob_inter_entrance_id` | First screen ad - reusable |
| **ON_BOARDING** | `true` | `null` | `admob_inter_on_boarding_id` | Onboarding screen - reusable |
| **DASHBOARD** | `true` | `1` | `admob_inter_dashboard_id` | Dashboard screen - reusable, parallel loading |
| **BOTTOM_NAVIGATION** | `false` | `1` | `admob_inter_bottom_navigation_id` | Bottom nav - NOT reusable, parallel loading |
| **BACK_PRESS** | `false` | `1` | `admob_inter_back_press_id` | Back press - NOT reusable, parallel loading |
| **EXIT** | `false` | `null` | `admob_inter_exit_id` | Exit screen - NOT reusable |

## Marketing Requirements & Test Scenarios

### Requirement 1: Best Show-Rates (Reuse Ads)
**Goal**: Minimize wasted ads (max 1 ad wasted per session)

#### Scenario 1.1: Entrance Ad Missed → Reuse for OnBoarding
```
Flow:
1. User opens app → ENTRANCE ad loads (reuseAd = true)
2. User navigates away quickly (ad not shown)
3. User reaches ON_BOARDING screen → Requests ad (reuseAd = true)
4. System finds ENTRANCE ad available → Reuses it
5. Result: ✅ 0 ads wasted, show-rate = 100%
```

**Expected Behavior:**
- `findReusableAd()` should find ENTRANCE ad
- `loadInterstitialAd(ON_BOARDING)` should skip loading, reuse ENTRANCE
- `showInterstitialAd(ON_BOARDING)` should show ENTRANCE ad

#### Scenario 1.2: Multiple Reusable Ads Available
```
Flow:
1. ENTRANCE ad loaded (not shown)
2. ON_BOARDING ad loaded (not shown)
3. DASHBOARD requests ad (reuseAd = true)
4. System should prioritize:
   a. Same adUnitId match (if ENTRANCE and DASHBOARD use same ID in release)
   b. First available ad (ENTRANCE or ON_BOARDING)
5. Result: ✅ Reuses available ad, no new load needed
```

**Expected Behavior:**
- `findReusableAd()` checks same adUnitId first
- If no same ID, returns first available reusable ad
- Skips ads that were already shown (`wasAdShown()`)

#### Scenario 1.3: All Reusable Ads Shown
```
Flow:
1. ENTRANCE ad loaded and shown
2. ON_BOARDING ad loaded and shown
3. DASHBOARD requests ad (reuseAd = true)
4. No reusable ads available (all shown)
5. System loads new DASHBOARD ad
6. Result: ⚠️ 1 ad wasted (acceptable per requirement)
```

**Expected Behavior:**
- `findReusableAd()` returns null (all reusable ads shown)
- `loadInterstitialAd(DASHBOARD)` loads new ad
- This is acceptable (max 1 ad wasted per session)

---

### Requirement 2: Per-Screen Revenue (No Reuse)
**Goal**: Track revenue per screen accurately

#### Scenario 2.1: Bottom Navigation Ad Must Show Only on Bottom Nav
```
Flow:
1. ENTRANCE ad loaded (reuseAd = true, available)
2. User clicks bottom navigation button
3. BOTTOM_NAVIGATION requests ad (reuseAd = false)
4. System should NOT reuse ENTRANCE ad
5. System loads new BOTTOM_NAVIGATION ad
6. Result: ✅ Revenue tracked correctly for bottom nav screen
```

**Expected Behavior:**
- `showInterstitialAd(BOTTOM_NAVIGATION)` ignores reusable ads
- Only shows BOTTOM_NAVIGATION ad (its own adUnitId)
- If not loaded, fails to show (doesn't reuse)

#### Scenario 2.2: Back Press Ad Must Show Only on Back Press
```
Flow:
1. DASHBOARD ad loaded (reuseAd = true, available)
2. User presses back button
3. BACK_PRESS requests ad (reuseAd = false)
4. System should NOT reuse DASHBOARD ad
5. System loads/shows BACK_PRESS ad only
6. Result: ✅ Revenue tracked correctly for back press event
```

**Expected Behavior:**
- `reuseAd = false` prevents reuse
- Only shows BACK_PRESS ad (its own adUnitId)
- Revenue attribution is accurate

#### Scenario 2.3: Exit Ad Must Show Only on Exit
```
Flow:
1. Multiple ads loaded (ENTRANCE, ON_BOARDING, DASHBOARD)
2. User exits app
3. EXIT requests ad (reuseAd = false)
4. System should NOT reuse any ad
5. System loads/shows EXIT ad only
6. Result: ✅ Revenue tracked correctly for exit event
```

**Expected Behavior:**
- `reuseAd = false` prevents reuse
- Only shows EXIT ad (its own adUnitId)
- Even if other ads available, doesn't reuse

---

### Requirement 3: Mixed Scenario (Some Reusable, Some Not)
**Goal**: Balance show-rates and revenue tracking

#### Scenario 3.1: Reusable Ads Share, Non-Reusable Are Isolated
```
Flow:
1. ENTRANCE ad loaded (reuseAd = true)
2. BOTTOM_NAVIGATION ad loaded (reuseAd = false)
3. ON_BOARDING requests ad (reuseAd = true)
4. System can reuse ENTRANCE, but NOT BOTTOM_NAVIGATION
5. Result: ✅ ENTRANCE reused, BOTTOM_NAVIGATION isolated
```

**Expected Behavior:**
- `findReusableAd()` only considers ads with `reuseAd = true`
- BOTTOM_NAVIGATION (reuseAd = false) is excluded from reuse pool
- Only ENTRANCE, ON_BOARDING, DASHBOARD can share

#### Scenario 3.2: Dashboard Can Reuse Entrance But Not Bottom Nav
```
Flow:
1. ENTRANCE loaded (reuseAd = true)
2. BOTTOM_NAVIGATION loaded (reuseAd = false)
3. DASHBOARD requests ad (reuseAd = true)
4. System reuses ENTRANCE, ignores BOTTOM_NAVIGATION
5. Result: ✅ Mixed behavior working correctly
```

**Expected Behavior:**
- DASHBOARD can reuse ENTRANCE (both reuseAd = true)
- DASHBOARD cannot reuse BOTTOM_NAVIGATION (reuseAd = false)
- Revenue tracking maintained for non-reusable ads

---

### Requirement 4: Parallel Loading
**Goal**: Load multiple ads simultaneously for dashboard features

#### Scenario 4.1: Dashboard Parallel Loading
```
Flow:
1. User on dashboard screen
2. System loads in parallel:
   - DASHBOARD ad (bufferSize = 1, reuseAd = true)
   - BOTTOM_NAVIGATION ad (bufferSize = 1, reuseAd = false)
   - BACK_PRESS ad (bufferSize = 1, reuseAd = false)
3. All ads load simultaneously
4. Result: ✅ All ads ready when needed
```

**Expected Behavior:**
- `bufferSize = 1` allows preloading while showing
- Multiple `loadInterstitialAd()` calls don't block each other
- Each ad type loads independently

#### Scenario 4.2: Feature Buttons on Dashboard
```
Flow:
1. Dashboard screen loads
2. FeatureOne button → Loads DASHBOARD ad
3. FeatureTwo button → Can reuse DASHBOARD ad (reuseAd = true)
4. Bottom nav buttons → Loads BOTTOM_NAVIGATION ad (reuseAd = false)
5. Back press → Loads BACK_PRESS ad (reuseAd = false)
6. Result: ✅ Parallel loading, ready on demand
```

**Expected Behavior:**
- DASHBOARD ad can be reused for FeatureOne/FeatureTwo
- BOTTOM_NAVIGATION and BACK_PRESS load separately (no reuse)
- All ads available when user interacts

#### Scenario 4.3: Buffer Size Impact
```
Flow:
1. DASHBOARD ad loaded (bufferSize = 1)
2. User clicks FeatureOne → Shows ad
3. While showing, system preloads next ad (bufferSize = 1)
4. User clicks FeatureTwo → Next ad ready
5. Result: ✅ Smooth experience, no loading delay
```

**Expected Behavior:**
- `bufferSize = 1` enables preloading during show
- Next ad ready immediately after current ad
- No user waiting for ad load

---

## Edge Cases & Potential Issues

### Edge Case 1: Ad Loaded But User Exits App
```
Scenario:
1. ENTRANCE ad loaded (reuseAd = true)
2. User exits app before showing
3. User returns, goes to ON_BOARDING
4. Question: Is ENTRANCE ad still available?

Expected:
- If app process killed: Ad lost, load new
- If app in background: Ad might still be available
- System should check `isInterstitialAvailable()` before reuse
```

### Edge Case 2: Ad Fails to Load
```
Scenario:
1. ENTRANCE ad loaded successfully (reuseAd = true)
2. ON_BOARDING ad fails to load
3. Question: Should ON_BOARDING reuse ENTRANCE?

Expected:
- `loadInterstitialAd()` should check for reusable ad first
- If reusable available, skip loading, reuse
- Only load new if no reusable available
```

### Edge Case 3: Same AdUnitId in Release
```
Scenario:
1. In release, ENTRANCE and ON_BOARDING use same adUnitId
2. ENTRANCE ad loaded
3. ON_BOARDING requests ad
4. Question: Should it reuse or load new?

Expected:
- `findReusableAd()` prioritizes same adUnitId match
- If same ID found and available, reuse it
- This maximizes show-rate (no duplicate loads)
```

### Edge Case 4: Ad Shown But Not Tracked
```
Scenario:
1. ENTRANCE ad shown (onAdImpression called)
2. System marks ad as shown (`wasAdShown = true`)
3. ON_BOARDING requests ad (reuseAd = true)
4. Question: Should it reuse already-shown ENTRANCE?

Expected:
- `wasAdShown()` should return true
- `findReusableAd()` should skip shown ads
- Load new ad instead of reusing shown ad
```

### Edge Case 5: Race Condition - Multiple Simultaneous Requests
```
Scenario:
1. ENTRANCE ad loaded (reuseAd = true)
2. ON_BOARDING and DASHBOARD both request ad simultaneously
3. Question: Which one gets to reuse ENTRANCE?

Expected:
- First request reuses ENTRANCE
- Second request finds no reusable ad, loads new
- ConcurrentHashMap ensures thread-safety
```

---

## Test Cases Summary

### Test Suite 1: getAdConfig - ReuseAd Configuration
- ✅ ENTRANCE: reuseAd = true
- ✅ ON_BOARDING: reuseAd = true
- ✅ DASHBOARD: reuseAd = true, bufferSize = 1
- ✅ BOTTOM_NAVIGATION: reuseAd = false, bufferSize = 1
- ✅ BACK_PRESS: reuseAd = false, bufferSize = 1
- ✅ EXIT: reuseAd = false, bufferSize = null

### Test Suite 2: Reuse Logic - Best Show-Rates
- ✅ ENTRANCE missed → Reusable for ON_BOARDING
- ✅ Multiple reusable ads → Prioritize same adUnitId
- ✅ Already shown ad → Not reusable

### Test Suite 3: Per-Screen Revenue - No Reuse
- ✅ BOTTOM_NAVIGATION doesn't reuse other ads
- ✅ BACK_PRESS doesn't reuse other ads
- ✅ EXIT doesn't reuse other ads

### Test Suite 4: Parallel Loading
- ✅ DASHBOARD bufferSize = 1
- ✅ Multiple ads load simultaneously
- ✅ BOTTOM_NAVIGATION bufferSize = 1
- ✅ BACK_PRESS bufferSize = 1

### Test Suite 5: Mixed Scenario
- ✅ Reusable ads share, non-reusable isolated
- ✅ DASHBOARD can reuse ENTRANCE but not BOTTOM_NAVIGATION

### Test Suite 6: Edge Cases
- ✅ Ad loaded but user exits app
- ✅ Ad fails to load, reusable available
- ✅ All reusable ads shown
- ✅ Same adUnitId in release

---

## Recommendations for Best Show-Rates

### Current Configuration Analysis:
1. **Reusable Ads (Best Show-Rates)**: ENTRANCE, ON_BOARDING, DASHBOARD ✅
2. **Non-Reusable Ads (Per-Screen Revenue)**: BOTTOM_NAVIGATION, BACK_PRESS, EXIT ✅
3. **Parallel Loading Support**: DASHBOARD, BOTTOM_NAVIGATION, BACK_PRESS (bufferSize = 1) ✅

### Potential Improvements:
1. **Consider making DASHBOARD non-reusable** if revenue tracking is critical for dashboard
2. **Consider making ON_BOARDING non-reusable** if onboarding revenue needs separate tracking
3. **Increase bufferSize for dashboard** if multiple features need ads (e.g., bufferSize = 3 for FeatureOne, FeatureTwo, FeatureThree)

### Show-Rate Optimization Tips:
1. **Load reusable ads early**: Load ENTRANCE on app start
2. **Preload next screen**: If user on ENTRANCE, preload ON_BOARDING
3. **Reuse aggressively**: For reusable ads, always check for available ads before loading
4. **Track ad availability**: Use `isInterstitialAdLoaded()` before showing to avoid failed shows

---

## Testing Instructions

1. **Unit Tests**: Run `InterstitialAdsConfigReuseTest.kt`
2. **Integration Tests**: Test actual ad loading/showing in debug mode
3. **Manual Testing**: 
   - Test reuse scenarios by loading ads and navigating
   - Test parallel loading by requesting multiple ads simultaneously
   - Test per-screen revenue by checking ad attribution

## Debug vs Release Considerations

- **Debug**: All ads use same test ad IDs (ca-app-pub-3940256099942544/...)
- **Release**: Each ad type uses different ad IDs
- **Impact**: In release, `findReusableAd()` with same adUnitId check is less relevant
- **Recommendation**: Focus on reuse logic that works regardless of adUnitId

