package com.example.androidvpn.ui

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wireguard.android.backend.Tunnel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToServerList: () -> Unit,
    onNavigateToSplitTunnel: () -> Unit = {}
) {
    val vpnState by viewModel.vpnState.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()
    val duration by viewModel.connectionDuration.collectAsState()
    val isProvisioning by viewModel.isProvisioning.collectAsState()
    val latencyMs by viewModel.latencyMs.collectAsState()

    // Start/stop latency monitor based on VPN state
    LaunchedEffect(vpnState, currentConfig?.endpoint) {
        if (vpnState == Tunnel.State.UP && currentConfig?.endpoint != null) {
            viewModel.startLatencyMonitor(currentConfig?.endpoint)
        } else {
            viewModel.stopLatencyMonitor()
        }
    }

    // VPN Permission Launcher
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.toggleVpn()
            }
        }
    )

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ ->
            val intent = viewModel.checkVpnPermission()
            if (intent != null) {
                vpnPermissionLauncher.launch(intent)
            } else {
                viewModel.toggleVpn()
            }
        }
    )

    val isConnected = vpnState == Tunnel.State.UP

    // Dynamic Colors based on State
    val statusColor = when {
        isProvisioning -> Color(0xFFFFA726)
        isConnected -> Color(0xFF00C853)
        else -> Color(0xFFFF5252)
    }
    val statusText = when {
        isProvisioning -> "SETTING UP..."
        isConnected -> "CONNECTED"
        else -> "DISCONNECTED"
    }
    val buttonText = when {
        isProvisioning -> "Setting up..."
        isConnected -> "STOP"
        else -> "CONNECT"
    }

    val onConnectClick: () -> Unit = {
        if (!isConnected) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                val intent = viewModel.checkVpnPermission()
                if (intent != null) {
                    vpnPermissionLauncher.launch(intent)
                } else {
                    viewModel.toggleVpn()
                }
            }
        } else {
            viewModel.toggleVpn()
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (isLandscape) {
            // ΓöÇΓöÇ LANDSCAPE: Two-column layout ΓöÇΓöÇ
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left: Status circle
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    StatusCircle(
                        isConnected = isConnected,
                        isProvisioning = isProvisioning,
                        statusColor = statusColor,
                        statusText = statusText,
                        duration = duration,
                        circleSize = 180
                    )
                }

                // Right: Controls (scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(end = 10.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Server card
                    ServerCard(
                        currentConfig = currentConfig,
                        latencyMs = latencyMs,
                        isProvisioning = isProvisioning,
                        onNavigateToServerList = onNavigateToServerList
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Connect button
                    ConnectButton(
                        buttonText = buttonText,
                        isProvisioning = isProvisioning,
                        isConnected = isConnected,
                        onClick = onConnectClick
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Settings buttons row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onNavigateToSplitTunnel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Settings, "Settings", modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Split Tunnel", fontSize = 12.sp)
                        }

                        AlwaysOnButton(modifier = Modifier.weight(1f), compact = true)
                    }
                }
            }
        } else {
            // ΓöÇΓöÇ PORTRAIT: Single-column layout ΓöÇΓöÇ
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = "Android VPN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status Circle
                StatusCircle(
                    isConnected = isConnected,
                    isProvisioning = isProvisioning,
                    statusColor = statusColor,
                    statusText = statusText,
                    duration = duration,
                    circleSize = 240
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Server card
                ServerCard(
                    currentConfig = currentConfig,
                    latencyMs = latencyMs,
                    isProvisioning = isProvisioning,
                    onNavigateToServerList = onNavigateToServerList
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Connect button
                ConnectButton(
                    buttonText = buttonText,
                    isProvisioning = isProvisioning,
                    isConnected = isConnected,
                    onClick = onConnectClick
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Split Tunnel button
                OutlinedButton(
                    onClick = onNavigateToSplitTunnel,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Icon(Icons.Filled.Settings, "Settings", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Split Tunneling")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Always-on VPN button
                AlwaysOnButton(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}


@Composable
private fun StatusCircle(
    isConnected: Boolean,
    isProvisioning: Boolean,
    statusColor: Color,
    statusText: String,
    duration: String,
    circleSize: Int
) {
    Box(
        modifier = Modifier
            .size(circleSize.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        statusColor.copy(alpha = 0.1f),
                        statusColor.copy(alpha = 0.05f)
                    )
                )
            )
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isProvisioning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(if (circleSize > 200) 64.dp else 48.dp),
                    color = statusColor,
                    strokeWidth = 4.dp
                )
            } else {
                Icon(
                    imageVector = if (isConnected) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(if (circleSize > 200) 64.dp else 48.dp)
                )
            }
            Spacer(modifier = Modifier.height(if (circleSize > 200) 16.dp else 8.dp))
            Text(
                text = statusText,
                fontSize = if (circleSize > 200) 20.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            if (isConnected) {
                Text(
                    text = duration,
                    fontSize = if (circleSize > 200) 16.sp else 13.sp,
                    color = statusColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (isProvisioning) {
                Text(
                    text = "Registering...",
                    fontSize = if (circleSize > 200) 14.sp else 11.sp,
                    color = statusColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ServerCard(
    currentConfig: com.example.androidvpn.model.ServerConfig?,
    latencyMs: Long?,
    isProvisioning: Boolean,
    onNavigateToServerList: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isProvisioning) onNavigateToServerList() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        // Server Info
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Selected Server",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
            if (currentConfig != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentConfig.endpoint,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    // Latency badge
                    val latencyText = when {
                        latencyMs == null -> "\u23F3"
                        latencyMs < 0 -> "--"
                        else -> "${latencyMs}ms"
                    }
                    val latencyColor = when {
                        latencyMs == null -> Color.Gray
                        latencyMs < 0 -> Color.Gray
                        latencyMs < 100 -> Color(0xFF00C853)
                        latencyMs < 200 -> Color(0xFFFFA726)
                        else -> Color(0xFFFF5252)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = latencyColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = latencyText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = latencyColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        // Latency badge
                        val latencyText = when {
                            latencyMs == null -> "⏳"
                            latencyMs < 0 -> "--"
                            else -> "${latencyMs}ms"
                        }
                        val latencyColor = when {
                            latencyMs == null -> Color.Gray
                            latencyMs < 0 -> Color.Gray
                            latencyMs < 100 -> Color(0xFF00C853) // Green
                            latencyMs < 200 -> Color(0xFFFFA726) // Orange
                            else -> Color(0xFFFF5252) // Red
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = latencyColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = latencyText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = latencyColor,
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}


// Connect Button

@Composable
private fun ConnectButton(
    buttonText: String,
    isProvisioning: Boolean,
    isConnected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isProvisioning -> Color(0xFFFFA726)
                isConnected -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
        ),
        shape = RoundedCornerShape(28.dp),
        enabled = !isProvisioning
    ) {
        if (isProvisioning) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Text(text = buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AlwaysOnButton(modifier: Modifier = Modifier, compact: Boolean = false) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            try {
                context.startActivity(Intent(android.provider.Settings.ACTION_VPN_SETTINGS))
            } catch (_: Exception) {
                context.startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 16.dp else 28.dp),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        else ButtonDefaults.ContentPadding
    ) {
        Icon(
            Icons.Filled.Lock,
            "Always-on",
            modifier = Modifier.size(if (compact) 16.dp else 18.dp)
        )
        Spacer(Modifier.width(if (compact) 4.dp else 8.dp))
        Column {
            Text("Always-on VPN", fontSize = if (compact) 12.sp else 14.sp)
            if (!compact) {
                Text(
                    "Auto-connect on boot",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
