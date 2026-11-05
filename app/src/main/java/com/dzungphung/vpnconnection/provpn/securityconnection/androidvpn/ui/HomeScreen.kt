package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.components.BannerAd
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToTerms: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isDetectionActive by viewModel.isDetectionActive.collectAsState()
    val isAlarmRinging by viewModel.isAlarmRinging.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val detectionMode by viewModel.detectionMode.collectAsState()
    val sensitivity by viewModel.sensitivity.collectAsState()
    val selectedSoundId by viewModel.selectedSoundId.collectAsState()
    val flashMode by viewModel.flashMode.collectAsState()
    val vibrationMode by viewModel.vibrationMode.collectAsState()
    val onlyWhenScreenOff by viewModel.onlyWhenScreenOff.collectAsState()

    var showPaywall by remember { mutableStateOf(false) }

    // Permission launcher for Record Audio & Post Notifications
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    if (isPremium) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "VIP PRO",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { showPaywall = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "VIP",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToTerms) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D12))
            )
        },
        bottomBar = {
            if (!isPremium) {
                BannerAd()
            }
        },
        containerColor = Color(0xFF0D0D12)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // -------------------------------------------------------------
            // ALARM RINGING OVERLAY / CARD
            // -------------------------------------------------------------
            AnimatedVisibility(visible = isAlarmRinging) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔔 " + stringResource(R.string.phone_found),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.stopAlarm() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.stop_alarm),
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // BIG ACTIVATION BUTTON (PULSING ANIMATION)
            // -------------------------------------------------------------
            Spacer(modifier = Modifier.height(8.dp))

            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = if (isDetectionActive) 1.08f else 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulseScale)
            ) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDetectionActive) Color(0xFF00E676).copy(alpha = 0.15f)
                            else Color(0xFF6200EA).copy(alpha = 0.15f)
                        )
                )
                // Inner button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(175.dp)
                        .clip(CircleShape)
                        .background(
                            brush = if (isDetectionActive) {
                                Brush.radialGradient(
                                    listOf(Color(0xFF00E676), Color(0xFF00A152))
                                )
                            } else {
                                Brush.radialGradient(
                                    listOf(Color(0xFF7C4DFF), Color(0xFF512DA8))
                                )
                            }
                        )
                        .clickable { checkAndToggle() }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isDetectionActive) Icons.Default.Hearing else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isDetectionActive) stringResource(R.string.tap_to_deactivate)
                            else stringResource(R.string.tap_to_activate),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isDetectionActive) stringResource(R.string.listening_active)
                else stringResource(R.string.finder_inactive),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDetectionActive) Color(0xFF00E676) else Color.Gray,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Test Alarm Button
            OutlinedButton(
                onClick = { viewModel.testAlarm() },
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAlarmRinging) Color(0xFFD32F2F) else Color(0xFF7C4DFF)
                )
            ) {
                Icon(
                    imageVector = if (isAlarmRinging) Icons.Default.Stop else Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isAlarmRinging) Color(0xFFD32F2F) else Color(0xFF7C4DFF),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAlarmRinging) stringResource(R.string.stop_alarm) else stringResource(R.string.test_alarm),
                    color = if (isAlarmRinging) Color(0xFFD32F2F) else Color(0xFF7C4DFF),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // DETECTION MODE PICKER
            // -------------------------------------------------------------
            SectionTitle(title = stringResource(R.string.detection_mode))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetectionMode.values().forEach { mode ->
                    val isSelected = (detectionMode == mode)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setDetectionMode(mode) },
                        label = {
                            Text(
                                text = stringResource(mode.displayNameRes),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6200EA),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------------------------------------
            // SOUND SELECTION (CAROUSEL)
            // -------------------------------------------------------------
            SectionTitle(title = stringResource(R.string.sound_selection))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AvailableSounds.list) { sound ->
                    val isSelected = (selectedSoundId == sound.id)
                    SoundCard(
                        sound = sound,
                        isSelected = isSelected,
                        isPremiumUser = isPremium,
                        onClick = {
                            if (sound.isVip && !isPremium) {
                                showPaywall = true
                            } else {
                                viewModel.selectSound(sound)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // ALERT SETTINGS: FLASH & VIBRATION
            // -------------------------------------------------------------
            SectionTitle(title = "Tùy chọn Cảnh báo")
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Flash Mode
                    Text(
                        text = stringResource(R.string.flash_mode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FlashMode.values().forEach { mode ->
                            val isSelected = (flashMode == mode)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFlashMode(mode) },
                                label = { Text(text = stringResource(mode.displayNameRes), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6200EA),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2A2A3E))

                    // Vibration Mode
                    Text(
                        text = stringResource(R.string.vibration_mode),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VibrationMode.values().forEach { mode ->
                            val isSelected = (vibrationMode == mode)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setVibrationMode(mode) },
                                label = { Text(text = stringResource(mode.displayNameRes), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6200EA),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2A2A3E))

                    // Sensitivity
                    Text(
                        text = stringResource(R.string.sensitivity),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SensitivityLevel.values().forEach { lvl ->
                            val isSelected = (sensitivity == lvl)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSensitivity(lvl) },
                                label = { Text(text = stringResource(lvl.displayNameRes), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6200EA),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------------------------------------------------
            // SMART BATTERY SAVER TOGGLE
            // -------------------------------------------------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.battery_saver_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.battery_saver_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = onlyWhenScreenOff,
                        onCheckedChange = { viewModel.setOnlyWhenScreenOff(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFAAAAAA),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun SoundCard(
    sound: SoundItem,
    isSelected: Boolean,
    isPremiumUser: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(110.dp)
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF6200EA).copy(alpha = 0.35f) else Color(0xFF1E1E2E),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Color(0xFF7C4DFF) else Color(0xFF2E2E3E)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            if (sound.isVip && !isPremiumUser) {
                Surface(
                    color = Color(0xFFFFD700),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "VIP",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = sound.iconEmoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(sound.nameRes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}
