package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.AvailableSounds
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.SoundCategory
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.SoundItem
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.HomeViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.components.SoundVisuals
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme.AppColors

@Composable
fun SoundStudioScreen(
    viewModel: HomeViewModel,
    onOpenPaywall: () -> Unit
) {
    val selectedSoundId by viewModel.selectedSoundId.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val volumePercent by viewModel.volumePercent.collectAsState()
    val alarmDurationSec by viewModel.alarmDurationSec.collectAsState()
    val previewSoundId by viewModel.previewSoundId.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPreview()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // -------------------------------------------------------------
        // OUTPUT CONTROLS: VOLUME & DURATION
        // -------------------------------------------------------------
        item {
            Surface(
                color = AppColors.Surface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = AppColors.AccentCoral,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.alarm_volume),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                        }
                        Text(
                            text = "$volumePercent%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.AccentCoral
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = volumePercent.toFloat(),
                        onValueChange = { viewModel.setVolumePercent(it.toInt()) },
                        valueRange = 20f..100f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = AppColors.AccentCoral,
                            activeTrackColor = AppColors.AccentCoral,
                            inactiveTrackColor = AppColors.SurfaceVariant
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = AppColors.SurfaceBorder
                    )

                    // Duration
                    Text(
                        text = stringResource(R.string.alarm_duration),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15 to "15s", 30 to "30s", 60 to "1m", 120 to "2m").forEach { (sec, label) ->
                            val isSelected = (alarmDurationSec == sec)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clickable { viewModel.setAlarmDurationSec(sec) },
                                color = if (isSelected) AppColors.AccentCoralSoft else AppColors.SurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) AppColors.AccentCoral else AppColors.SurfaceBorder
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) AppColors.AccentCoral else AppColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // SOUND ITEMS GROUPED BY CATEGORY
        // -------------------------------------------------------------
        SoundCategory.values().forEach { category ->
            val soundsInCategory = AvailableSounds.list.filter { it.category == category }
            if (soundsInCategory.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(category.titleRes).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(soundsInCategory) { sound ->
                    val isSelected = (selectedSoundId == sound.id)
                    val isPreviewing = (previewSoundId == sound.id)
                    val visual = remember(sound.id) { SoundVisuals.getVisual(sound.id) }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (sound.isVip && !isPremium) {
                                    onOpenPaywall()
                                } else {
                                    viewModel.selectSound(sound)
                                }
                            },
                        color = if (isSelected) Color(0xFF22283A) else AppColors.Surface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) AppColors.AccentCoral else AppColors.SurfaceBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(visual.bgTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = visual.icon,
                                        contentDescription = null,
                                        tint = visual.iconTint,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(sound.nameRes),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = AppColors.TextPrimary
                                        )
                                        if (sound.isVip && !isPremium) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = AppColors.AccentAmber,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "VIP",
                                                    color = Color.Black,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isSelected) "Active Alarm Sound" else "Tap to choose",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) AppColors.AccentEmerald else AppColors.TextMuted
                                    )
                                }
                            }

                            // Preview Button
                            IconButton(
                                onClick = {
                                    if (sound.isVip && !isPremium) {
                                        onOpenPaywall()
                                    } else {
                                        viewModel.previewSound(sound)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPreviewing) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                                    contentDescription = stringResource(R.string.preview_sound),
                                    tint = if (isPreviewing) AppColors.AccentCoral else AppColors.TextSecondary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

