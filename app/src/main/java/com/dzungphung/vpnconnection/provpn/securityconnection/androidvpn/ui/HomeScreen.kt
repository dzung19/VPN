package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.BuildConfig
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.components.BannerAd
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.components.AlarmRingingOverlay
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.screens.CalibrationLabScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.screens.FinderDashboardScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.screens.SettingsScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.screens.SoundStudioScreen
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme.AppColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTerms: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isAlarmRinging by viewModel.isAlarmRinging.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val selectedSoundId by viewModel.selectedSoundId.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showPaywall by remember { mutableStateOf(false) }

    // Audio & Notification Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (audioGranted) {
            viewModel.toggleDetection(context)
        }
    }

    fun checkAndToggle() {
        val hasAudio = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (hasAudio) {
            viewModel.toggleDetection(context)
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    if (showPaywall) {
        PaywallDialog(
            price = viewModel.formattedPrice,
            onDismiss = { showPaywall = false },
            onPurchase = {
                activity?.let { viewModel.buyVip(it) }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth().background(AppColors.Surface)) {
                    NavigationBar(
                        containerColor = AppColors.Surface,
                        contentColor = AppColors.TextPrimary,
                        tonalElevation = 8.dp,
                        modifier = Modifier.height(68.dp)
                    ) {
                        // Tab 0: Finder
                        NavigationBarItem(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            icon = { Icon(Icons.Default.Sensors, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_finder), fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.AccentEmerald,
                                selectedTextColor = AppColors.AccentEmerald,
                                indicatorColor = AppColors.AccentEmeraldSoft,
                                unselectedIconColor = AppColors.TextMuted,
                                unselectedTextColor = AppColors.TextMuted
                            )
                        )

                        // Tab 1: Sounds
                        NavigationBarItem(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            icon = { Icon(Icons.Default.GraphicEq, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_sounds), fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.AccentCoral,
                                selectedTextColor = AppColors.AccentCoral,
                                indicatorColor = AppColors.AccentCoralSoft,
                                unselectedIconColor = AppColors.TextMuted,
                                unselectedTextColor = AppColors.TextMuted
                            )
                        )

                        // Tab 2: Calibration Lab
                        NavigationBarItem(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_calibration), fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.AccentBlue,
                                selectedTextColor = AppColors.AccentBlue,
                                indicatorColor = AppColors.AccentBlueSoft,
                                unselectedIconColor = AppColors.TextMuted,
                                unselectedTextColor = AppColors.TextMuted
                            )
                        )

                        // Tab 3: Settings
                        NavigationBarItem(
                            selected = selectedTabIndex == 3,
                            onClick = { selectedTabIndex = 3 },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text(stringResource(R.string.tab_settings), fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.AccentAmber,
                                selectedTextColor = AppColors.AccentAmber,
                                indicatorColor = AppColors.AccentAmberSoft,
                                unselectedIconColor = AppColors.TextMuted,
                                unselectedTextColor = AppColors.TextMuted
                            )
                        )
                    }

                    // Banner Ad
                    if (!isPremium && !BuildConfig.ADS_DISABLED) {
                        BannerAd(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        )
                    }
                }
            },
            containerColor = AppColors.Background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTabIndex) {
                    0 -> FinderDashboardScreen(
                        viewModel = viewModel,
                        onCheckAndToggle = { checkAndToggle() },
                        onNavigateToTab = { tab -> selectedTabIndex = tab },
                        onOpenPaywall = { showPaywall = true }
                    )
                    1 -> SoundStudioScreen(
                        viewModel = viewModel,
                        onOpenPaywall = { showPaywall = true }
                    )
                    2 -> CalibrationLabScreen(
                        viewModel = viewModel
                    )
                    3 -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToTerms = onNavigateToTerms
                    )
                }
            }
        }

        // Full Screen Alarm Overlay when phone is triggered
        AlarmRingingOverlay(
            isAlarmRinging = isAlarmRinging,
            selectedSoundId = selectedSoundId,
            onStopAlarm = { viewModel.stopAlarm() }
        )
    }
}
