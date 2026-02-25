package com.example.androidvpn.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.androidvpn.model.ServerConfig
import com.wireguard.android.backend.Tunnel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerListScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val serverList by viewModel.serverList.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    var showPaywall by remember { mutableStateOf(false) }

    // Paywall dialog
    if (showPaywall) {
        PaywallDialog(
            billingManager = viewModel.billingManager,
            onDismiss = { showPaywall = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select Location") },
                navigationIcon = {
                    // Back button could go here
                }
            )
        }
        // fab removed as we use pre-defined list now
    ) { padding ->
        if (serverList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // FREE section
                item {
                    Text(
                        text = "FREE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
                item {
                    val isWarpSelected = currentConfig?.name == "Cloudflare WARP"
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWarpSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isWarpSelected) {
                                    viewModel.createCloudflareConfig()
                                }
                                onNavigateBack()
                            },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "\uD83C\uDF10", fontSize = 32.sp, modifier = Modifier.padding(end = 16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cloudflare WARP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    text = "Nearest location • Free",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isWarpSelected) {
                                Icon(Icons.Filled.Check, "Selected", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // PREMIUM section
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "PREMIUM",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isPremium) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "\uD83D\uDD12",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                items(serverList) { server ->
                    val isSelected =
                        currentConfig?.name == "${server.flag} ${server.city}"

                    ServerItemCard(
                        server = server,
                        isSelected = isSelected,
                        isLocked = !isPremium,
                        onClick = {
                            if (!isPremium) {
                                showPaywall = true
                            } else {
                                if (!isSelected) {
                                    viewModel.connectToServer(server)
                                }
                                onNavigateBack()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ServerItemCard(
    server: com.example.androidvpn.model.ServerItemDto,
    isSelected: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use local calculation for flag to avoid API encoding issues
            val flagEmoji = remember(server.id) {
                val code = server.id.take(2).uppercase()
                calculateFlagEmoji(code)
            }

            Text(
                text = flagEmoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.country,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Text(
                    text = server.city,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isLocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Premium",
                    tint = Color(0xFFFFD700)
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Helper to convert country code (e.g. "US") to Flag Emoji (≡ƒç║≡ƒç╕)
fun calculateFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "\uD83C\uDF10"
    val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

