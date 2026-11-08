package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.AvailableSounds
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.HomeViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.components.SoundVisuals
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme.AppColors

@Composable
fun FinderDashboardScreen(
    viewModel: HomeViewModel,
    onCheckAndToggle: () -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onOpenPaywall: () -> Unit
) {
    val isDetectionActive by viewModel.isDetectionActive.collectAsState()
    val isAlarmRinging by viewModel.isAlarmRinging.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val detectionMode by viewModel.detectionMode.collectAsState()
    val sensitivity by viewModel.sensitivity.collectAsState()
    val selectedSoundId by viewModel.selectedSoundId.collectAsState()
    val flashMode by viewModel.flashMode.collectAsState()
    val vibrationMode by viewModel.vibrationMode.collectAsState()
    val alarmDurationSec by viewModel.alarmDurationSec.collectAsState()
    val liveAudioLevel by viewModel.liveAudioLevel.collectAsState()

    val currentSound = remember(selectedSoundId) { AvailableSounds.getById(selectedSoundId) }
    val soundVisual = remember(selectedSoundId) { SoundVisuals.getVisual(selectedSoundId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // -----------------------------------------------------------------
        // HEADER ROW: TITLE + STATUS CHIP + VIP BADGE
        // -----------------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDetectionActive) AppColors.AccentEmerald
                                else AppColors.TextMuted
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDetectionActive) stringResource(R.string.status_active)
                        else stringResource(R.string.status_standby),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDetectionActive) AppColors.AccentEmerald else AppColors.TextMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (isPremium) {
                Surface(
                    color = AppColors.AccentAmberSoft,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.AccentAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AppColors.AccentAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VIP PRO",
                            color = AppColors.AccentAmber,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.clickable { onOpenPaywall() },
                    color = AppColors.AccentAmber,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VIP",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -----------------------------------------------------------------
        // HERO: TACTILE POWER DIAL WITH LIVE AUDIO PULSE
        // -----------------------------------------------------------------
        val infiniteTransition = rememberInfiniteTransition(label = "powerPulse")
        val breathScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = if (isDetectionActive) 1.05f else 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dialBreath"
        )

        // Dynamic scale reactive to live microphone energy when active
        val dynamicScale = if (isDetectionActive) {
            breathScale + (liveAudioLevel * 0.12f)
        } else 1.0f

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(230.dp)
                .scale(dynamicScale)
        ) {
            // Layer 1: Ambient Halo Glow
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDetectionActive) AppColors.AccentEmeraldSoft
                        else Color(0x15FFFFFF)
                    )
            )

            // Layer 2: Bezel Ring
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(AppColors.SurfaceVariant)
                    .border(
                        width = 2.dp,
                        brush = Brush.radialGradient(
                            if (isDetectionActive) listOf(AppColors.AccentEmerald, Color(0xFF065F46))
                            else listOf(AppColors.SurfaceBorder, Color(0xFF151821))
                        ),
                        shape = CircleShape
                    )
            )

            // Layer 3: Main Tactile Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(165.dp)
                    .clip(CircleShape)
                    .background(
                        brush = if (isDetectionActive) {
                            Brush.verticalGradient(
                                listOf(Color(0xFF10B981), Color(0xFF047857))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xFF262C3A), Color(0xFF131720))
                            )
                        }
                    )
                    .clickable { onCheckAndToggle() }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isDetectionActive) Icons.Default.Hearing else Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isDetectionActive) stringResource(R.string.listening_active)
                        else stringResource(R.string.tap_to_activate),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // -----------------------------------------------------------------
        // LIVE SENSOR METER STRIP
        // -----------------------------------------------------------------
        Surface(
            color = AppColors.Surface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (isDetectionActive) AppColors.AccentEmerald else AppColors.TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDetectionActive) "Sensor Listening" else "Sensor Idle",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // LED bar meter (5 segments)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val activeBars = if (isDetectionActive) {
                        ((liveAudioLevel * 5).toInt() + 1).coerceIn(1, 5)
                    } else 0

                    repeat(5) { index ->
                        val barLit = index < activeBars
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        !barLit -> AppColors.SurfaceVariant
                                        index >= 4 -> AppColors.AccentRed
                                        index >= 3 -> AppColors.AccentAmber
                                        else -> AppColors.AccentEmerald
                                    }
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------------------------------------------
        // BENTO GRID SUMMARY (2x2 TILES)
        // -----------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.quick_sound),
                value = stringResource(currentSound.nameRes),
                subtitle = "${viewModel.volumePercent.collectAsState().value}%",
                icon = soundVisual.icon,
                iconTint = soundVisual.iconTint,
                bgTint = soundVisual.bgTint,
                onClick = { onNavigateToTab(1) } // Navigate to Sounds
            )

            BentoTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.quick_mode),
                value = stringResource(detectionMode.displayNameRes),
                subtitle = stringResource(sensitivity.displayNameRes),
                icon = Icons.Default.Sensors,
                iconTint = AppColors.AccentBlue,
                bgTint = AppColors.AccentBlueSoft,
                onClick = { onNavigateToTab(2) } // Navigate to Lab
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BentoTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.quick_flash),
                value = stringResource(flashMode.displayNameRes),
                subtitle = "Camera Torch",
                icon = Icons.Default.FlashlightOn,
                iconTint = AppColors.AccentAmber,
                bgTint = AppColors.AccentAmberSoft,
                onClick = { onNavigateToTab(3) } // Navigate to Settings
            )

            BentoTile(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.quick_haptics),
                value = stringResource(vibrationMode.displayNameRes),
                subtitle = "${alarmDurationSec}s",
                icon = Icons.Default.Vibration,
                iconTint = AppColors.AccentCoral,
                bgTint = AppColors.AccentCoralSoft,
                onClick = { onNavigateToTab(3) } // Navigate to Settings
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------------------------------------------
        // QUICK TEST BUTTON
        // -----------------------------------------------------------------
        OutlinedButton(
            onClick = { viewModel.testAlarm() },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = AppColors.Surface,
                contentColor = if (isAlarmRinging) AppColors.AccentRed else AppColors.TextPrimary
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAlarmRinging) AppColors.AccentRed else AppColors.SurfaceBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = if (isAlarmRinging) Icons.Default.Stop else Icons.Default.VolumeUp,
                contentDescription = null,
                tint = if (isAlarmRinging) AppColors.AccentRed else AppColors.AccentCoral,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isAlarmRinging) stringResource(R.string.stop_alarm)
                else stringResource(R.string.test_alarm),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun BentoTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    bgTint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(105.dp)
            .clickable { onClick() },
        color = AppColors.Surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

