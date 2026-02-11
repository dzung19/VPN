package com.example.androidvpn.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@Composable
fun ServerListScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit
) {
    // In a real app, we'd list all saved configs. 
    // For now, we'll just show the current one and an option to create the default Cloudflare one.
    
    val currentConfig by viewModel.currentConfig.collectAsState()
    val configs by viewModel.configs.collectAsState()
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Add Server")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Servers",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(configs) { config ->
                     ServerItem(
                         config = config,
                         isSelected = config.name == currentConfig?.name,
                         onClick = { 
                             viewModel.selectConfig(config)
                             onNavigateBack() // Optional: go back to home on selection
                         }
                     )
                }
            }
            
            if (configs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No servers found. Add one!")
                }
            }
        }
    }
}

@Composable
fun ServerItem(config: ServerConfig, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = config.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Text(
                text = config.endpoint,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
