package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.AddServerScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.HomeScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.ServerListScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.SplitTunnelScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.WalletScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.TermsOfServiceScreen

import dagger.hilt.android.AndroidEntryPoint
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.HomeViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // precise TOS acceptance state
        val prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE)
        val isTosAccepted = prefs.getBoolean("is_tos_accepted", false)
        val startDest = if (isTosAccepted) "home" else "tos"
        enableEdgeToEdge()
        val DarkColors = androidx.compose.material3.darkColorScheme(
            background = androidx.compose.ui.graphics.Color(0xFF0D0D12),
            surface = androidx.compose.ui.graphics.Color(0xFF1A1A2E),
            surfaceVariant = androidx.compose.ui.graphics.Color(0xFF252538),
            onBackground = androidx.compose.ui.graphics.Color.White,
            onSurface = androidx.compose.ui.graphics.Color.White,
            onSurfaceVariant = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f),
            primary = androidx.compose.ui.graphics.Color(0xFF6200EA),
            primaryContainer = androidx.compose.ui.graphics.Color(0xFF3700B3),
            onPrimary = androidx.compose.ui.graphics.Color.White,
            secondary = androidx.compose.ui.graphics.Color(0xFF03DAC6),
            onSecondary = androidx.compose.ui.graphics.Color.Black
        )

        setContent {
            MaterialTheme(colorScheme = DarkColors) {
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
                            val sharedViewModel: HomeViewModel = hiltViewModel(backStackEntry)
                            HomeScreen(
                                viewModel = sharedViewModel,
                                onNavigateToServerList = { navController.navigate("server_list") },
                                onNavigateToSplitTunnel = { navController.navigate("split_tunnel") },
                                onNavigateToWallet = { navController.navigate("wallet") }
                            )
                        }
                        composable("server_list") {
                            // Share the same ViewModel from "home" backstack entry
                            val parentEntry = remember(it) {
                                navController.getBackStackEntry("home")
                            }
                            val sharedViewModel: HomeViewModel = hiltViewModel(parentEntry)
                            ServerListScreen(
                                viewModel = sharedViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToWallet = { navController.navigate("wallet") }
                            )
                        }
                        composable("add_server") {
                            AddServerScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("split_tunnel") {
                            SplitTunnelScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("wallet") {
                            WalletScreen(
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
            false
    }
}
