package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Memory-efficient icon cache for installed apps.
 *
 * Uses LruCache to keep only recently-viewed icons in RAM, automatically
 * evicting the oldest entries when the cache is full. Icons are loaded
 * lazily on a background thread — never eagerly into a data class.
 */
@Singleton
class AppIconCache @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    // Use 1/8th of available app memory for the icon cache
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // Size in KB
            return bitmap.byteCount / 1024
        }
    }

    /**
     * Returns the cached icon for [packageName], or loads it from
     * PackageManager on Dispatchers.IO if not cached yet.
     */
    suspend fun getIcon(packageName: String): Bitmap? {
        // 1. Check cache first (cache hit)
        cache.get(packageName)?.let { return it }

        // 2. Cache miss — load from PackageManager on IO thread
        return withContext(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val drawable = appInfo.loadIcon(pm)
                val bitmap = drawable?.toBitmap() // Full resolution, LruCache handles eviction
                if (bitmap != null) {
                    cache.put(packageName, bitmap) // Store in cache
                }
                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    fun clear() {
        cache.evictAll()
    }
}
