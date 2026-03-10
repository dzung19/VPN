# Unify WARP (Free) + Server API (Paid) in Server List

WARP currently auto-creates via `CloudflareService` on first launch but isn't in the server list. Server API locations (US/Iowa, etc.) appear in the list but use a separate flow. We'll merge them so the user sees **one unified server list** with WARP as the free default.

## Proposed Changes

### Worker API

#### [MODIFY] [worker.js](file:///c:/Users/phung/New%20folder/AndroidVPN/server/worker.js)

Add a **Cloudflare WARP** entry to the `/api/servers` response as a free-tier option:

```diff
 const servers = [
+    {
+        id: 'cf-warp',
+        country: 'Cloudflare',
+        city: 'WARP',
+        flag: '🌐',
+        premium: false,
+        maxUsers: 0,
+    },
     {
         id: 'us-iowa',
         ...
-        premium: false,
+        premium: true,
         ...
     },
```

- `us-iowa` becomes `premium: true` (paid tier)
- `cf-warp` is `premium: false` (free tier)

---

### Android App — Repository

#### [MODIFY] [ServerRepository.kt](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/example/androidvpn/data/ServerRepository.kt)

Update [connectToServer()](file:///C:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/data/ServerRepository.kt#136-185) to detect the `cf-warp` server ID and route to the existing `CloudflareService` flow instead of the Worker `/api/connect`:

```diff
 suspend fun connectToServer(serverItem: ServerItemDto): ServerConfig? = withContext(Dispatchers.IO) {
+    // Route WARP through CloudflareService
+    if (serverItem.id == "cf-warp") {
+        return@withContext connectToWarp(serverItem)
+    }
     val keys = getOrCreateClientKeys()
     ...
 }

+private suspend fun connectToWarp(serverItem: ServerItemDto): ServerConfig? {
+    // Reuse existing CloudflareService logic
+    val keys = getOrCreateClientKeys()
+    val config = cloudflareService.registerAndGetConfig(
+        keys.privateKey.toBase64(), keys.publicKey.toBase64()
+    ) ?: return null
+    // Override name/metadata to match server list display
+    val warpConfig = config.copy(
+        name = "${serverItem.flag} ${serverItem.city}",
+        country = serverItem.country,
+        flag = serverItem.flag,
+        city = serverItem.city,
+        isPremium = false
+    )
+    addConfig(warpConfig)
+    saveCurrentConfig(warpConfig)
+    return warpConfig
+}
```

---

### Android App — ViewModel

#### [MODIFY] [HomeViewModel.kt](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/example/androidvpn/ui/HomeViewModel.kt)

- Remove the auto-creation of Cloudflare config in [loadData()](file:///C:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/ui/HomeViewModel.kt#80-91) (lines 72-75)
- Remove [createCloudflareConfig()](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/ui/HomeViewModel.kt#92-166) method (lines 79-87)
- The server list UI now handles everything — user picks WARP or a paid server from the same list

---

### Android App — Server List UI

#### [MODIFY] [ServerListScreen.kt](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/example/androidvpn/ui/ServerListScreen.kt)

Update `ServerItemCard` to show a **"FREE"** badge for non-premium servers and a **"PRO"** / crown badge for premium, making the tier distinction clear visually.

## Verification Plan

### Manual Verification
1. Build the app: `./gradlew assembleDebug` from project root
2. Install on a device, clear app data to remove old "Cloudflare WARP" configs
3. Open Server List → verify "Cloudflare WARP" appears as free, "US Iowa" and "Singapore" appear as premium
4. Select WARP → verify it connects using Cloudflare WARP endpoint (`162.159.192.1:2408`)
5. Select US Iowa → verify it connects using the GCP VM endpoint

# Monetization via Passes

We will introduce a "Wallet" or balance system using Google Play Billing consumable passes instead of a typical monthly subscription.

## Product Tiers
1. **Data Passes**: "1 GB", "3 GB", "5 GB", "7 GB", "10 GB"
2. **Time Passes**: "12 Hour" ($0.60), "24 Hour" ($1.20)

## Proposed Changes

### 1. [PassModels.kt](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/example/androidvpn/model/PassModels.kt) (New)
Define models `DataPass` and [TimePass](file:///C:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/data/WalletManager.kt#47-56) that represent individual inventory items and their depletion calculations. 

### 2. [WalletManager.kt](file:///C:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/data/WalletManager.kt) / `WalletRepository.kt`
Create a centralized source of truth backed by `SharedPreferences` or `Room` containing:
- Current available `totalBytes`
- Current `bytesConsumed`
- Active Pass Ends At (Timestamp `Long?`)

### 3. [HomeViewModel.kt](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/example/androidvpn/ui/HomeViewModel.kt) & [TunnelManager.kt](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/example/androidvpn/data/TunnelManager.kt) Integration
- Track bytes dynamically from WireGuard's properties inside `TunnelManager` -> `speedMonitorJob`.
- Deduct the bytes sent/received from [WalletManager](file:///C:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/data/WalletManager.kt#13-57).
- Terminate the tunnel automatically over a `StateFlow` event from [WalletManager](file:///C:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/data/WalletManager.kt#13-57) if:
   a) `remainingDataBytes` drops to 0.
   b) `timePassActiveUntil` has expired.

### 4. [WalletScreen.kt](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/example/androidvpn/ui/WalletScreen.kt) & Google Play Billing
- Add `com.android.billingclient:billing` to Gradle.
- Connect to Google Play console's product IDs.
- Present pass cards allowing users to buy data or time and immediately credit their [WalletManager](file:///C:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/data/WalletManager.kt#13-57).

### 5. [ServerListScreen.kt](file:///c:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/example/androidvpn/ui/ServerListScreen.kt) Integration
- Instead of outright denying click for `premium: true` servers, present a dialogue asking "Use 12H Time Pass or 500MB from balance?" if no active passes are open, otherwise redirect them to buy passes from the Wallet.

# User Interface Revamp

The app currently uses a standard Material 3 implementation. We will enhance the visual appeal to make it look like a premium VPN product.

## Proposed Changes

### 1. Animated Status Circle
The static circular background for the lock icon will be replaced with an `InfiniteTransition` that smoothly pulses and scales (like a sonar or breathing radar) when the VPN is in a "Connecting" or "Connected" state.

### 2. Glassmorphism Design
- Update the background of the main content box from a flat color to an immersive vertical gradient (e.g., deep navy to soft neon accents) that changes color states entirely when the VPN connects (e.g., smoothly shifting to an emerald/mint gradient).
- [ServerCard](file:///C:/Users/phung/New%20folder/AndroidVPN/app/src/main/java/com/dzungphung/vpnconnection/provpn/securityconnection/androidvpn/ui/HomeScreen.kt#500-602) and utility buttons will get frosted glass effects (white with 0.15 alpha) with subtle background blurs and thin borders (`0.5.dp`), moving away from solid blocks of color.

### 3. "Wallet" Floating Action Button (FAB)
Move the "Wallet & Passes" text out of a cramped secondary button row and into a prominent, floating action button located at the bottom right of the screen. This FAB can eventually house an animated circular progress bar showing the user's remaining data allocation directly on the home screen.

### 4. Micro-interactions
- Add `AnimatedContent` so the "DISCONNECTED" to "CONNECTED" text slides gracefully into place instead of abruptly swapping out.
- Add `HapticFeedback` to buttons and server selection cards so the app feels tactile.

## Verification Plan
1. Create a `StatusButton` independent composable to verify the radar animation speed.
2. Build the app and confirm the gradient animations handle system light/dark mode changes correctly.
3. Verify HapticFeedback fires on tap interactions.
