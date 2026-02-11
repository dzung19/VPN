package com.example.androidvpn.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var configText by remember { mutableStateOf("") }
    var serverName by remember { mutableStateOf("New Server") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Server") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = serverName,
                onValueChange = { serverName = it },
                label = { Text("Server Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = configText,
                onValueChange = { configText = it },
                label = { Text("Paste WireGuard Config (Interface + Peer)") },
                modifier = Modifier.fillMaxWidth().height(300.dp),
                minLines = 10
            )

            Button(
                onClick = {
                    if (serverName.isNotEmpty() && configText.isNotEmpty()) {
                        viewModel.parseAndAddConfig(serverName, configText)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Custom Config")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text("Quick Setup", style = MaterialTheme.typography.titleMedium)
            
            Button(
                onClick = {
                    viewModel.createCloudflareConfig()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Create Free Cloudflare WARP Profile")
            }
        }
    }
}
