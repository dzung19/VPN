package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.FlashMode
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.VibrationMode
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.HomeViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme.AppColors

@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onNavigateToTerms: () -> Unit
) {
    val context = LocalContext.current
    val flashMode by viewModel.flashMode.collectAsState()
    val vibrationMode by viewModel.vibrationMode.collectAsState()
    val onlyWhenScreenOff by viewModel.onlyWhenScreenOff.collectAsState()

    val hasMic = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // -------------------------------------------------------------
        // FLASH & VIBRATION HARDWARE CONTROLS
        // -------------------------------------------------------------
        SectionHeader(text = "HARDWARE ALERTS")

        Surface(
            color = AppColors.Surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppColors.SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                // Flashlight Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = null,
                            tint = AppColors.AccentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.flash_mode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }

                    TextButton(
                        onClick = { viewModel.testFlashPreview(flashMode) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.test_flash),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.AccentAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FlashMode.values().forEach { mode ->
                        val isSelected = (flashMode == mode)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable { viewModel.setFlashMode(mode) },
                            color = if (isSelected) AppColors.AccentAmberSoft else AppColors.SurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AppColors.AccentAmber else AppColors.SurfaceBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(mode.displayNameRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AppColors.AccentAmber else AppColors.TextSecondary
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = AppColors.SurfaceBorder
                )

                // Vibration Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = AppColors.AccentCoral,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.vibration_mode),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }

                    TextButton(
                        onClick = { viewModel.testVibrationPreview(vibrationMode) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.test_vibe),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.AccentCoral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VibrationMode.values().forEach { mode ->
                        val isSelected = (vibrationMode == mode)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable { viewModel.setVibrationMode(mode) },
                            color = if (isSelected) AppColors.AccentCoralSoft else AppColors.SurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AppColors.AccentCoral else AppColors.SurfaceBorder
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(mode.displayNameRes),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AppColors.AccentCoral else AppColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // SMART BATTERY SAVER
        // -------------------------------------------------------------
        SectionHeader(
            text = "BATTERY & OPTIMIZATION",
            modifier = Modifier.padding(top = 4.dp)
        )

        Surface(
            color = AppColors.Surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppColors.SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = AppColors.AccentEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.battery_saver_title),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.battery_saver_desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = onlyWhenScreenOff,
                    onCheckedChange = { viewModel.setOnlyWhenScreenOff(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.AccentEmerald,
                        checkedTrackColor = AppColors.AccentEmeraldSoft,
                        uncheckedTrackColor = AppColors.SurfaceVariant
                    )
                )
            }
        }

        // -------------------------------------------------------------
        // SYSTEM HEALTH & PERMISSIONS
        // -------------------------------------------------------------
        SectionHeader(
            text = stringResource(R.string.system_permissions).uppercase(),
            modifier = Modifier.padding(top = 4.dp)
        )

        Surface(
            color = AppColors.Surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppColors.SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)) {
                // Mic Permission
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (hasMic) AppColors.AccentEmerald else AppColors.AccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.perm_mic),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextPrimary
                        )
                    }

                    Text(
                        text = if (hasMic) stringResource(R.string.perm_granted) else stringResource(R.string.perm_missing),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hasMic) AppColors.AccentEmerald else AppColors.AccentRed
                    )
                }

                HorizontalDivider(color = AppColors.SurfaceBorder)

                // Notification Permission
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = if (hasNotif) AppColors.AccentEmerald else AppColors.AccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.perm_notif),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextPrimary
                        )
                    }

                    Text(
                        text = if (hasNotif) stringResource(R.string.perm_granted) else stringResource(R.string.perm_missing),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (hasNotif) AppColors.AccentEmerald else AppColors.AccentRed
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // ABOUT & LEGAL
        // -------------------------------------------------------------
        SectionHeader(
            text = stringResource(R.string.about_and_legal).uppercase(),
            modifier = Modifier.padding(top = 4.dp)
        )

        Surface(
            color = AppColors.Surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppColors.SurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTerms() }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AppColors.AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.privacy_policy),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(color = AppColors.SurfaceBorder)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTerms() }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = AppColors.AccentCoral,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.terms_of_service),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Version Label
        Text(
            text = stringResource(R.string.app_version_label),
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextMuted,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = AppColors.TextMuted,
        letterSpacing = 1.sp,
        modifier = modifier
    )
}
