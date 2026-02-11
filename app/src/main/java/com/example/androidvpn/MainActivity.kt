package com.example.androidvpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            com.example.androidvpn.ui.HomeScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                            )
                            // Temporary overlay for navigation
                            Box(modifier = Modifier.fillMaxSize()) {
                                Button(
                                    onClick = { navController.navigate("server_list") },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                                ) {
                                    Text("Servers")
                                }
                            }
                        }
                        composable("server_list") {
                            com.example.androidvpn.ui.ServerListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAdd = { navController.navigate("add_server") }
                            )
                        }
                        composable("add_server") {
                            com.example.androidvpn.ui.AddServerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
