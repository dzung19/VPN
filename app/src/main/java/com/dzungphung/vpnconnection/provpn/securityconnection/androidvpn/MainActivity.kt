package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.HomeScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.HomeViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.TermsOfServiceScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow waking screen and showing over lock screen when alarm is triggered
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val isTosAccepted = prefs.getBoolean("is_tos_accepted", false)
        val startDest = if (isTosAccepted) "home" else "tos"

        enableEdgeToEdge()

        val DarkColors = androidx.compose.material3.darkColorScheme(
            background = Color(0xFF0D0D12),
            surface = Color(0xFF1A1A2E),
            surfaceVariant = Color(0xFF252538),
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Color.White.copy(alpha = 0.7f),
            primary = Color(0xFF6200EA),
            primaryContainer = Color(0xFF3700B3),
            onPrimary = Color.White,
            secondary = Color(0xFF00E676),
            onSecondary = Color.Black
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
                            val viewModel: HomeViewModel = hiltViewModel(backStackEntry)
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToTerms = {
                                    navController.navigate("tos")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
    }
}
