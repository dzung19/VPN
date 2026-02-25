package com.example.androidvpn.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isExcluded: Boolean
)

@Singleton
class SplitTunnelRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("split_tunnel", Context.MODE_PRIVATE)
    private val KEY_EXCLUDED = "excluded_apps_csv"

    fun getExcludedApps(): Set<String> {
        val csv = prefs.getString(KEY_EXCLUDED, "") ?: ""
        val apps = if (csv.isBlank()) emptySet() else csv.split(",").toSet()
        android.util.Log.d("SplitTunnel", "getExcludedApps: ${apps.size} -> $apps")
        return apps
    }

    private fun saveExcludedApps(apps: Set<String>) {
        val csv = apps.joinToString(",")
        prefs.edit().putString(KEY_EXCLUDED, csv).commit()
        android.util.Log.d("SplitTunnel", "saveExcludedApps: ${apps.size} saved -> $csv")
    }

    fun toggleApp(packageName: String) {
        val current = getExcludedApps().toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
            android.util.Log.d("SplitTunnel", "REMOVED: $packageName")
        } else {
            current.add(packageName)
            android.util.Log.d("SplitTunnel", "ADDED: $packageName")
        }
        saveExcludedApps(current)
    }

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val excluded = getExcludedApps()
        val ownPackage = context.packageName

        // Use launcher intent query ΓÇö works with <queries> in manifest, no QUERY_ALL_PACKAGES needed
        val launcherIntent = android.content.Intent(android.content.Intent.ACTION_MAIN, null).apply {
            addCategory(android.content.Intent.CATEGORY_LAUNCHER)
        }

        pm.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolveInfo ->
                val appInfo = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                val packageName = appInfo.packageName
                if (packageName == ownPackage) return@mapNotNull null

                AppInfo(
                    packageName = packageName,
                    appName = appInfo.loadLabel(pm).toString(),
                    isExcluded = excluded.contains(packageName)
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(compareByDescending<AppInfo> { it.isExcluded }.thenBy { it.appName })
    }
}
