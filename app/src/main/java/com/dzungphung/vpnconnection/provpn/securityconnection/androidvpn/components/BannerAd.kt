package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.components

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.daumo.ads.DynamicAdsManager
import kotlinx.coroutines.delay

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Create a reference to the ad container
    val adContainer = remember { FrameLayout(context) }
    
    // Load the banner ad when the composable is first composed
    LaunchedEffect(Unit) {
        try {
            Log.d("BannerAd", "Starting banner ad initialization")
            // Add a small delay to ensure the DynamicAdsManager is initialized
            delay(100)
            
            val activity = context as? Activity
            if (activity != null) {
                Log.d("BannerAd", "Activity found, attempting to load banner ad")
                // Try to get the monetization manager with a retry mechanism
                var attempts = 0
                while (attempts < 5) {
                    Log.d("BannerAd", "Attempt ${attempts + 1} to get monetization manager")
                    val monetizationManager = try {
                        DynamicAdsManager.getInstance().getMonetizationManager()
                    } catch (e: Exception) {
                        Log.e("BannerAd", "Error getting monetization manager", e)
                        null
                    }
                    
                    if (monetizationManager != null) {
                        Log.d("BannerAd", "Monetization manager found, loading banner ad")
                        monetizationManager.loadBannerAd(
                            activity = activity,
                            container = adContainer
                        )
                        Log.d("BannerAd", "Banner ad load requested")
                        break
                    } else {
                        Log.d("BannerAd", "Monetization manager not available, retrying...")
                        attempts++
                        delay(200)
                    }
                }
                if (attempts >= 5) {
                    Log.e("BannerAd", "Failed to get monetization manager after 5 attempts")
                }
            } else {
                Log.e("BannerAd", "Context is not an Activity")
            }
        } catch (e: Exception) {
            Log.e("BannerAd", "Error loading banner ad", e)
        }
    }
    
    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                Log.d("BannerAd", "onResume called")
                try {
                    val monetizationManager = try {
                        DynamicAdsManager.getInstance().getMonetizationManager()
                    } catch (e: Exception) {
                        Log.e("BannerAd", "Error getting monetization manager in onResume", e)
                        null
                    }
                    if (monetizationManager != null) {
                        Log.d("BannerAd", "Resuming banner ad")
                        monetizationManager.resumeBannerAd()
                    } else {
                        Log.d("BannerAd", "Monetization manager not available in onResume")
                    }
                } catch (e: Exception) {
                    Log.e("BannerAd", "Error resuming banner ad", e)
                }
            }
            
            override fun onPause(owner: LifecycleOwner) {
                Log.d("BannerAd", "onPause called")
                try {
                    val monetizationManager = try {
                        DynamicAdsManager.getInstance().getMonetizationManager()
                    } catch (e: Exception) {
                        Log.e("BannerAd", "Error getting monetization manager in onPause", e)
                        null
                    }
                    if (monetizationManager != null) {
                        Log.d("BannerAd", "Pausing banner ad")
                        monetizationManager.pauseBannerAd()
                    } else {
                        Log.d("BannerAd", "Monetization manager not available in onPause")
                    }
                } catch (e: Exception) {
                    Log.e("BannerAd", "Error pausing banner ad", e)
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            Log.d("BannerAd", "onDispose called")
            try {
                lifecycleOwner.lifecycle.removeObserver(observer)
                val monetizationManager = try {
                    DynamicAdsManager.getInstance().getMonetizationManager()
                } catch (e: Exception) {
                    Log.e("BannerAd", "Error getting monetization manager in onDispose", e)
                    null
                }
                if (monetizationManager != null) {
                    Log.d("BannerAd", "Destroying banner ad")
                    monetizationManager.destroyBannerAd()
                } else {
                    Log.d("BannerAd", "Monetization manager not available in onDispose")
                }
            } catch (e: Exception) {
                Log.e("BannerAd", "Error destroying banner ad", e)
            }
        }
    }
    
    // Create the banner ad container
    Row(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .navigationBarsPadding().then(modifier), // 10dp padding from bottom to avoid user click confusion
    ) {
        AndroidView(
            factory = { adContainer },
            modifier = Modifier.fillMaxSize().align(Alignment.CenterVertically),
        )
    }
}
