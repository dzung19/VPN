package com.example.androidvpn.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wireguard.android.backend.Tunnel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen

import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToServerList: () -> Unit
) {
    val vpnState by viewModel.vpnState.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()
    val duration by viewModel.connectionDuration.collectAsState()
    val isProvisioning by viewModel.isProvisioning.collectAsState()
    val latencyMs by viewModel.latencyMs.collectAsState()
    
    // Measure latency when config changes
    LaunchedEffect(currentConfig?.endpoint) {
        currentConfig?.endpoint?.let { viewModel.measureLatency(it) }
    }
    
    val context = LocalContext.current
    
    // VPN Permission Launcher
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.toggleVpn()
            }
        }
    )

    val isConnected = vpnState == Tunnel.State.UP
    
    // Dynamic Colors based on State
    val statusColor = when {
        isProvisioning -> Color(0xFFFFA726) // Orange for provisioning
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Text(
            text = "Android VPN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 24.dp)
        )

        // Status Circle
        Box(
            modifier = Modifier
                .size(240.dp)
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
                        modifier = Modifier.size(64.dp),
                        color = statusColor,
                        strokeWidth = 4.dp
                    )
                } else {
                    Icon(
                        imageVector = if(isConnected) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(64.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = statusText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                if (isConnected) {
                    Text(
                        text = duration,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (isProvisioning) {
                    Text(
                        text = "Registering with server...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Server Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable {
                    if (!isProvisioning) onNavigateToServerList()
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Selected Server", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(onClick = { if (!isProvisioning) onNavigateToServerList() }) {
                        Text("Change")
                    }
                }
                
                Text(
                    text = currentConfig?.name ?: "Select a Server",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (currentConfig != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentConfig!!.endpoint,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        // Latency badge
                        val latencyText = when {
                            latencyMs == null -> "⏳"
                            latencyMs!! < 0 -> "--"
                            else -> "${latencyMs}ms"
                        }
                        val latencyColor = when {
                            latencyMs == null -> Color.Gray
                            latencyMs!! < 0 -> Color.Gray
                            latencyMs!! < 100 -> Color(0xFF00C853) // Green
                            latencyMs!! < 200 -> Color(0xFFFFA726) // Orange
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
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // Proceed regardless (User can deny notifications, VPN still works but silent)
            val intent = viewModel.checkVpnPermission()
            if (intent != null) {
                vpnPermissionLauncher.launch(intent)
            } else {
                viewModel.toggleVpn()
            }
        }
    )

    // Connect Button
    Button(
        onClick = {
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
        },
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
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
