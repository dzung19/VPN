package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitTunnelScreen(
    viewModel: SplitTunnelViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val apps by viewModel.apps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val hasPremiumAccess by viewModel.hasPremiumAccess.collectAsState()

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
                        text = "Excluded: $excludedCount apps",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
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
                    items(
                        items = filteredApps,
                        key = { it.packageName },
                        contentType = { "AppItem" }
                    ) { appInfo ->
                        AppItem(
                            appInfo = appInfo,
                            onToggle = { packageName ->
                                viewModel.toggleApp(packageName)
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
    onToggle: (String) -> Unit
) {
    val isChecked = remember(appInfo.isExcluded) { mutableStateOf(appInfo.isExcluded) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon
        appInfo.icon?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
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

        Switch(
            checked = isChecked.value,
            onCheckedChange = { 
                isChecked.value = it
                onToggle(appInfo.packageName) 
            }
        )
    }
}
