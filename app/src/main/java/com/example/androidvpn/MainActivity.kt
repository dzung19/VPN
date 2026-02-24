package com.example.androidvpn

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidvpn.ui.AddServerScreen
import com.example.androidvpn.ui.HomeScreen
import com.example.androidvpn.ui.ServerListScreen
import com.example.androidvpn.ui.TermsOfServiceScreen

import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.edit
import androidx.core.view.WindowCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // precise TOS acceptance state
        val prefs = getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)
        val isTosAccepted = prefs.getBoolean("is_tos_accepted", false)
        val startDest = if (isTosAccepted) "home" else "tos"
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = startDest) {
                        composable("tos") {
                            TermsOfServiceScreen(
                                onAccepted = {
                                    prefs.edit { putBoolean("is_tos_accepted", true) }
                                    navController.navigate("home") {
                                        popUpTo("tos") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") { backStackEntry ->
                            val sharedViewModel: com.example.androidvpn.ui.HomeViewModel = hiltViewModel(backStackEntry)
                            HomeScreen(
                                viewModel = sharedViewModel,
                                onNavigateToServerList = { navController.navigate("server_list") }
                            )
                        }
                        composable("server_list") {
                            // Share the same ViewModel from "home" backstack entry
                            val parentEntry = remember(it) {
                                navController.getBackStackEntry("home")
                            }
                            val sharedViewModel: com.example.androidvpn.ui.HomeViewModel = hiltViewModel(parentEntry)
                            ServerListScreen(
                                viewModel = sharedViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToAdd = { navController.navigate("add_server") }
                            )
                        }
                        composable("add_server") {
                            AddServerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            true
    }
}
