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
    val statusColor = if (isConnected) Color(0xFF00C853) else Color(0xFFFF5252)
    val statusText = if (isConnected) "CONNECTED" else "DISCONNECTED"
    val buttonText = if (isConnected) "STOP" else "CONNECT"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                Icon(
                    imageVector = if(isConnected) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(64.dp)
                )
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
            }
        }

        // Server Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable {
                    onNavigateToServerList()
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Selected Server", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onNavigateToServerList() }) {
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
                   Text(
                       text = currentConfig!!.endpoint,
                       fontSize = 12.sp,
                       color = MaterialTheme.colorScheme.onSurfaceVariant
                   )
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
             containerColor = if(isConnected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(text = buttonText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
