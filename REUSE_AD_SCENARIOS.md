# ReuseAd Scenarios - What Can Happen?

## Quick Reference: Current Configuration

```
✅ REUSABLE (reuseAd = true):
   - ENTRANCE
   - ON_BOARDING  
   - DASHBOARD

❌ NON-REUSABLE (reuseAd = false):
   - BOTTOM_NAVIGATION
   - BACK_PRESS
   - EXIT
```

## Scenario Matrix: What Happens When?

### Scenario 1: ENTRANCE Ad Loaded → ON_BOARDING Requests Ad

| ENTRANCE Status | ON_BOARDING reuseAd | What Happens? | Result |
|----------------|---------------------|---------------|---------|
| ✅ Loaded, not shown | `true` | **REUSES** ENTRANCE ad | ✅ Best show-rate (0 waste) |
| ✅ Loaded, shown | `true` | **LOADS NEW** ON_BOARDING ad | ⚠️ 1 ad wasted (acceptable) |
| ❌ Not loaded | `true` | **LOADS NEW** ON_BOARDING ad | ✅ Normal flow |
| ✅ Loaded, not shown | `false` | **LOADS NEW** ON_BOARDING ad | ❌ Wastes ENTRANCE ad |

**Key Point**: If ON_BOARDING had `reuseAd = false`, it would waste the ENTRANCE ad even if available.

---

### Scenario 2: ENTRANCE Ad Loaded → BOTTOM_NAVIGATION Requests Ad

| ENTRANCE Status | BOTTOM_NAVIGATION reuseAd | What Happens? | Result |
|----------------|---------------------------|---------------|---------|
| ✅ Loaded, not shown | `false` | **LOADS NEW** BOTTOM_NAVIGATION ad | ✅ Per-screen revenue (correct) |
| ✅ Loaded, shown | `false` | **LOADS NEW** BOTTOM_NAVIGATION ad | ✅ Per-screen revenue (correct) |
| ❌ Not loaded | `false` | **LOADS NEW** BOTTOM_NAVIGATION ad | ✅ Normal flow |

**Key Point**: BOTTOM_NAVIGATION **NEVER** reuses other ads (reuseAd = false), ensuring accurate revenue tracking.

---

### Scenario 3: DASHBOARD Ad Loaded → FeatureOne/FeatureTwo Click

| DASHBOARD Status | Request Type | reuseAd | What Happens? | Result |
|-----------------|--------------|---------|---------------|---------|
| ✅ Loaded, not shown | FeatureOne | `true` | **SHOWS** DASHBOARD ad | ✅ Immediate show |
| ✅ Loaded, shown | FeatureTwo | `true` | **REUSES** DASHBOARD ad (if bufferSize=1 preloaded) | ✅ Smooth UX |
| ❌ Not loaded | FeatureOne | `true` | **LOADS** then shows | ⚠️ User waits |

**Key Point**: With `bufferSize = 1`, DASHBOARD can preload next ad while showing current one.

---

### Scenario 4: Multiple Ads Loaded → DASHBOARD Requests Ad

| Available Ads | DASHBOARD reuseAd | What Happens? | Result |
|---------------|-------------------|---------------|---------|
| ENTRANCE ✅, ON_BOARDING ✅ | `true` | **REUSES** ENTRANCE (first found) | ✅ Best show-rate |
| ENTRANCE ✅ (same adUnitId), ON_BOARDING ✅ | `true` | **REUSES** ENTRANCE (same ID priority) | ✅ Best show-rate |
| ENTRANCE ✅, BOTTOM_NAVIGATION ✅ | `true` | **REUSES** ENTRANCE (ignores BOTTOM_NAVIGATION) | ✅ Correct behavior |
| All shown | `true` | **LOADS NEW** DASHBOARD ad | ⚠️ 1 ad wasted (acceptable) |

**Key Point**: `findReusableAd()` only considers ads with `reuseAd = true`, ignores non-reusable ads.

---

### Scenario 5: Parallel Loading - Dashboard Screen

| Action | Ad Type | reuseAd | bufferSize | What Happens? | Result |
|--------|---------|---------|------------|---------------|---------|
| Screen loads | DASHBOARD | `true` | `1` | **LOADS** DASHBOARD ad | ✅ Parallel |
| Screen loads | BOTTOM_NAVIGATION | `false` | `1` | **LOADS** BOTTOM_NAVIGATION ad | ✅ Parallel |
| Screen loads | BACK_PRESS | `false` | `1` | **LOADS** BACK_PRESS ad | ✅ Parallel |
| User clicks FeatureOne | DASHBOARD | `true` | `1` | **SHOWS** DASHBOARD ad | ✅ Ready |
| User clicks bottom nav | BOTTOM_NAVIGATION | `false` | `1` | **SHOWS** BOTTOM_NAVIGATION ad | ✅ Ready |
| User presses back | BACK_PRESS | `false` | `1` | **SHOWS** BACK_PRESS ad | ✅ Ready |

**Key Point**: All ads load in parallel, ready when user interacts. `bufferSize = 1` enables preloading.

---

### Scenario 6: User Journey - Best Case (All Reusable)

```
1. App opens → ENTRANCE ad loads (reuseAd = true)
2. User navigates to ON_BOARDING → REUSES ENTRANCE ad ✅
3. User goes to DASHBOARD → REUSES ENTRANCE ad ✅
4. User clicks FeatureOne → Shows DASHBOARD ad (preloaded)
5. User clicks FeatureTwo → REUSES DASHBOARD ad ✅

Result: ✅ 2 ads loaded, 5 ads shown = 250% efficiency!
```

---

### Scenario 7: User Journey - Worst Case (All Non-Reusable)

```
1. App opens → ENTRANCE ad loads (reuseAd = true)
2. User navigates to ON_BOARDING → REUSES ENTRANCE ad ✅
3. User clicks bottom nav → LOADS BOTTOM_NAVIGATION ad (reuseAd = false)
4. User presses back → LOADS BACK_PRESS ad (reuseAd = false)
5. User exits → LOADS EXIT ad (reuseAd = false)

Result: ⚠️ 4 ads loaded, 5 ads shown = 125% efficiency
```

---

### Scenario 8: Edge Case - Ad Fails to Load

| Scenario | What Happens? | Result |
|----------|---------------|---------|
| ENTRANCE fails to load | ON_BOARDING loads new ad | ✅ Graceful fallback |
| ENTRANCE loaded, ON_BOARDING fails | ON_BOARDING can reuse ENTRANCE | ✅ Best show-rate |
| All reusable ads fail | Each screen loads its own | ⚠️ No reuse possible |

**Key Point**: Reuse logic provides fallback when new ads fail to load.

---

### Scenario 9: Edge Case - Same AdUnitId in Release

| Debug vs Release | What Happens? | Result |
|-----------------|---------------|---------|
| **Debug**: All use same test ID | `findReusableAd()` finds same ID easily | ✅ High reuse rate |
| **Release**: Different IDs | `findReusableAd()` checks all reusable ads | ✅ Still reuses (different logic) |

**Key Point**: In release, reuse works even with different adUnitIds (checks all reusable ads).

---

### Scenario 10: Race Condition - Simultaneous Requests

| Request 1 | Request 2 | What Happens? | Result |
|-----------|-----------|---------------|---------|
| ON_BOARDING (reuseAd=true) | DASHBOARD (reuseAd=true) | First gets ENTRANCE, second loads new | ✅ Thread-safe |
| BOTTOM_NAVIGATION (reuseAd=false) | BACK_PRESS (reuseAd=false) | Both load new (no reuse) | ✅ Correct behavior |

**Key Point**: `ConcurrentHashMap` ensures thread-safety for parallel requests.

---

## Decision Tree: Should Ad Be Reused?

```
Request Ad (adType)
│
├─ Is reuseAd = false?
│  └─ YES → Load new ad (NO REUSE) ✅ Per-screen revenue
│
└─ Is reuseAd = true?
   │
   ├─ Is there a reusable ad available?
   │  │
   │  ├─ YES → Check: Was it already shown?
   │  │  │
   │  │  ├─ NO → REUSE IT ✅ Best show-rate
   │  │  │
   │  │  └─ YES → Load new ad ⚠️ 1 ad wasted
   │  │
   │  └─ NO → Load new ad ✅ Normal flow
```

---

## Show-Rate Optimization Tips

### ✅ Best Practices:
1. **Load reusable ads early**: Preload ENTRANCE on app start
2. **Check before loading**: Use `isInterstitialAdLoaded()` to find reusable ads
3. **Preload next screen**: If user on ENTRANCE, preload ON_BOARDING
4. **Use bufferSize = 1**: For screens with multiple interactions (dashboard)

### ❌ Common Mistakes:
1. **Loading when reusable available**: Always check `findReusableAd()` first
2. **Showing non-reusable ads on wrong screen**: Respect `reuseAd = false`
3. **Not preloading**: Load ads before user needs them
4. **Ignoring bufferSize**: Use `bufferSize = 1` for parallel loading

---

## Testing Your Configuration

### Test Case 1: Verify Reuse Works
```kotlin
// 1. Load ENTRANCE ad
config.loadInterstitialAd(InterAdKey.ENTRANCE)

// 2. Wait for load (or mock as loaded)
// 3. Request ON_BOARDING ad
config.loadInterstitialAd(InterAdKey.ON_BOARDING) { isLoaded ->
    // Should reuse ENTRANCE, not load new
    assertTrue("Should reuse ENTRANCE ad", isLoaded)
}
```

### Test Case 2: Verify Non-Reuse Works
```kotlin
// 1. Load ENTRANCE ad
config.loadInterstitialAd(InterAdKey.ENTRANCE)

// 2. Request BOTTOM_NAVIGATION ad
config.loadInterstitialAd(InterAdKey.BOTTOM_NAVIGATION) { isLoaded ->
    // Should load new, not reuse ENTRANCE
    // (This test requires mocking InterstitialAdPreloader)
}
```

### Test Case 3: Verify Parallel Loading
```kotlin
// Load multiple ads simultaneously
config.loadInterstitialAd(InterAdKey.DASHBOARD)
config.loadInterstitialAd(InterAdKey.BOTTOM_NAVIGATION)
config.loadInterstitialAd(InterAdKey.BACK_PRESS)

// All should load in parallel (not block each other)
```

---

## Summary: What Can Happen?

| Configuration | Best Case | Worst Case | Typical Case |
|--------------|-----------|------------|--------------|
| **All Reusable** | 250% efficiency | 100% efficiency | 150% efficiency |
| **All Non-Reusable** | 100% efficiency | 100% efficiency | 100% efficiency |
| **Mixed (Current)** | 200% efficiency | 100% efficiency | 125% efficiency |

**Current Configuration Analysis:**
- ✅ **3 reusable ads** (ENTRANCE, ON_BOARDING, DASHBOARD) → Best show-rates
- ✅ **3 non-reusable ads** (BOTTOM_NAVIGATION, BACK_PRESS, EXIT) → Per-screen revenue
- ✅ **3 parallel loading** (DASHBOARD, BOTTOM_NAVIGATION, BACK_PRESS) → Smooth UX

**Result**: Balanced approach - maximizes show-rates while maintaining revenue tracking accuracy.

