package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.DetectionMode
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.SensitivityLevel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.HomeViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme.AppColors

@Composable
fun CalibrationLabScreen(
    viewModel: HomeViewModel
) {
    val detectionMode by viewModel.detectionMode.collectAsState()
    val sensitivity by viewModel.sensitivity.collectAsState()
    val isDetectionActive by viewModel.isDetectionActive.collectAsState()
    val liveAudioLevel by viewModel.liveAudioLevel.collectAsState()
    val lastAiConfidence by viewModel.lastAiConfidence.collectAsState()

    val animatedAudioLevel by animateFloatAsState(
        targetValue = liveAudioLevel,
        label = "audioMeterAnim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // -------------------------------------------------------------
        // LIVE SENSOR & AI CALIBRATION GAUGE
        // -------------------------------------------------------------
        Surface(
            color = AppColors.Surface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = AppColors.AccentEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.live_sensor),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }

                    Surface(
                        color = if (isDetectionActive) AppColors.AccentEmeraldSoft else AppColors.SurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isDetectionActive) "MIC RUNNING" else "MIC PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isDetectionActive) AppColors.AccentEmerald else AppColors.TextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Acoustic Peak Meter
                Text(
                    text = "Acoustic Energy Meter",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.SurfaceVariant)
                ) {
                    // Meter Fill
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedAudioLevel.coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    animatedAudioLevel > 0.6f -> AppColors.AccentRed
                                    animatedAudioLevel > 0.3f -> AppColors.AccentCoral
                                    else -> AppColors.AccentEmerald
                                }
                            )
                    )

                    // Target Threshold Marker
                    val thresholdPercent = when (sensitivity) {
                        SensitivityLevel.HIGH -> 0.18f
                        SensitivityLevel.MEDIUM -> 0.25f
                        SensitivityLevel.LOW -> 0.35f
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(thresholdPercent)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0 dB (Silence)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "▲ Trigger Threshold",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "+100 dB",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextMuted,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // AI Confidence Meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ai_confidence),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        text = "${(lastAiConfidence * 100).toInt()}% match",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (lastAiConfidence > 0.3f) AppColors.AccentEmerald else AppColors.TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { lastAiConfidence.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AppColors.AccentBlue,
                    trackColor = AppColors.SurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.test_clap_prompt),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // -------------------------------------------------------------
        // DETECTION MODE SELECTION
        // -------------------------------------------------------------
        Text(
            text = stringResource(R.string.detection_mode).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMuted,
            letterSpacing = 1.sp
        )

        DetectionMode.values().forEach { mode ->
            val isSelected = (detectionMode == mode)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setDetectionMode(mode) },
                color = if (isSelected) Color(0xFF202738) else AppColors.Surface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) AppColors.AccentBlue else AppColors.SurfaceBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) AppColors.AccentBlueSoft
                                    else AppColors.SurfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (mode) {
                                    DetectionMode.CLAP_ONLY -> Icons.Default.PanTool
                                    DetectionMode.WHISTLE_ONLY -> Icons.Default.GraphicEq
                                    DetectionMode.CLAP_AND_WHISTLE -> Icons.Default.Sensors
                                },
                                contentDescription = null,
                                tint = if (isSelected) AppColors.AccentBlue else AppColors.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = stringResource(mode.displayNameRes),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = AppColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(mode.descRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.setDetectionMode(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AppColors.AccentBlue,
                            unselectedColor = AppColors.TextMuted
                        )
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // SENSITIVITY CALIBRATION
        // -------------------------------------------------------------
        Text(
            text = stringResource(R.string.sensitivity).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMuted,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SensitivityLevel.values().forEach { lvl ->
                val isSelected = (sensitivity == lvl)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setSensitivity(lvl) },
                    color = if (isSelected) AppColors.AccentCoralSoft else AppColors.Surface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) AppColors.AccentCoral else AppColors.SurfaceBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(lvl.displayNameRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AppColors.AccentCoral else AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (lvl) {
                                SensitivityLevel.LOW -> "Noisy Room"
                                SensitivityLevel.MEDIUM -> "Balanced"
                                SensitivityLevel.HIGH -> "Long Range"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

