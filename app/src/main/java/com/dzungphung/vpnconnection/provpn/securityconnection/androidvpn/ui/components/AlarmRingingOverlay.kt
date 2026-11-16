package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.AvailableSounds
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme.AppColors

@Composable
fun AlarmRingingOverlay(
    isAlarmRinging: Boolean,
    selectedSoundId: String,
    onStopAlarm: () -> Unit
) {
    AnimatedVisibility(
        visible = isAlarmRinging,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(250))
    ) {
        val currentSound = remember(selectedSoundId) { AvailableSounds.getById(selectedSoundId) }

        val infiniteTransition = rememberInfiniteTransition(label = "alarmWaves")
        val waveScale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wave"
        )
        val waveAlpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "alpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF00D0F14))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onStopAlarm
                )
                .systemBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Radar Rings
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(190.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(waveScale)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = waveAlpha))
                    )
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.phone_found),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(currentSound.nameRes),
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Giant Stop Button
                Button(
                    onClick = onStopAlarm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.stop_alarm),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.tap_anywhere_to_stop),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
