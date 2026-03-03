package com.example.androidvpn.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val dataBalance by viewModel.remainingDataBytes.collectAsState()
    val timePassActiveUntil by viewModel.timePassActiveUntilMs.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallet & Passes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header: Current Balances
            item {
                BalanceCard(dataBalance, timePassActiveUntil)
            }

            item {
                Text("Available Passes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            }

            items(availableProducts) { product ->
                ProductCard(product) {
                    viewModel.purchaseProduct(product.id)
                }
            }
        }
    }
}

@Composable
fun BalanceCard(dataBalance: Long, timePassActiveUntil: Long?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Current Balance", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            val formattedData = if (dataBalance > 1024 * 1024 * 1024) {
                String.format("%.1f GB", dataBalance / (1024f * 1024f * 1024f))
            } else {
                String.format("%.1f MB", dataBalance / (1024f * 1024f))
            }
            
            Text("Data: $formattedData", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            
            if (timePassActiveUntil != null && timePassActiveUntil > System.currentTimeMillis()) {
                val remainingMs = timePassActiveUntil - System.currentTimeMillis()
                val hours = remainingMs / (1000 * 60 * 60)
                val mins = (remainingMs / (1000 * 60)) % 60
                Text("Active Pass: ${hours}h ${mins}m remaining", color = MaterialTheme.colorScheme.primary)
            } else {
                Text("No active time passes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun ProductCard(product: StoreProduct, onPurchaseClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(product.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onPurchaseClick) {
                Text(product.price)
            }
        }
    }
}

data class StoreProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: String
)
