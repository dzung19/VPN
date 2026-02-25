package com.example.androidvpn.ui

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.androidvpn.data.AppInfo
import com.example.androidvpn.data.SplitTunnelRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitTunnelScreen(
    repository: SplitTunnelRepository,
    onNavigateBack: () -> Unit
) {
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Load apps
    LaunchedEffect(Unit) {
        apps = repository.getInstalledApps()
        isLoading = false
    }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) apps
        else apps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
    }

    val excludedCount = apps.count { it.isExcluded }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Split Tunneling") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Excluded apps will bypass the VPN",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "$excludedCount app${if (excludedCount != 1) "s" else ""} excluded",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { appInfo ->
                        AppItem(
                            appInfo = appInfo,
                            onToggle = {
                                android.util.Log.d("SplitTunnel", "=== TOGGLE CLICKED: ${appInfo.packageName} ===")
                                repository.toggleApp(appInfo.packageName)
                                // Verify the save worked by reading back
                                val verify = repository.getExcludedApps()
                                android.util.Log.d("SplitTunnel", "=== VERIFY after toggle: ${verify.size} -> $verify ===")
                                // Update local state
                                apps = apps.map { app ->
                                    if (app.packageName == appInfo.packageName)
                                        app.copy(isExcluded = !app.isExcluded)
                                    else app
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItem(
    appInfo: AppInfo,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val appIcon: Drawable? = remember(appInfo.packageName) {
        try {
            context.packageManager.getApplicationIcon(appInfo.packageName)
        } catch (e: Exception) {
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon
        appIcon?.let { drawable ->
            Image(
                bitmap = drawable.toBitmap(48, 48).asImageBitmap(),
                contentDescription = appInfo.appName,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // App name and package
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = appInfo.appName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        // Toggle
        Switch(
            checked = appInfo.isExcluded,
            onCheckedChange = { onToggle() }
        )
    }
}
