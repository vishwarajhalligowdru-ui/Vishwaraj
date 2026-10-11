package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ControlMode
import com.example.data.HighScoreRepository

@Composable
fun TitleScreen(
    highScore: Int,
    repository: HighScoreRepository,
    onStartGame: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var controlMode by remember { mutableStateOf(repository.getControlMode()) }
    var autoFire by remember { mutableStateOf(repository.isAutoFireEnabled()) }
    var soundEnabled by remember { mutableStateOf(repository.isSoundEnabled()) }
    var hapticsEnabled by remember { mutableStateOf(repository.isHapticsEnabled()) }
    var showHowToPlay by remember { mutableStateOf(false) }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val buttonScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btnScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x99050914))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // TITLE LOGO & SUBTITLE
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = Color(0x3300E5FF),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8800E5FF)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "SPACE DEFENSE DIVISION",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Text(
                    text = "ASTROSTRIKE",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Hyperdrive Arcade Shooter",
                    color = Color(0xFF90CAF9),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // HIGH SCORE HERO CARD
                Surface(
                    color = Color(0x441E2640),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFD600)),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🏆", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "PERSONAL BEST",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$highScore PTS",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenLeaderboard,
                            modifier = Modifier.testTag("leaderboard_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Leaderboard,
                                contentDescription = "View Leaderboard",
                                tint = Color(0xFFFFD54F)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LAUNCH MISSION PRIMARY BUTTON
            Button(
                onClick = onStartGame,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(64.dp)
                    .scale(buttonScale)
                    .testTag("start_game_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF050914)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LAUNCH MISSION",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SETTINGS & CONTROLS CONFIGURATION
            Surface(
                color = Color(0x3310162F),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4437474F)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FLIGHT CONTROLS & SETTINGS",
                        color = Color(0xFF90CAF9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control Mode Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Steering Mode",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = controlMode.title,
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp
                            )
                        }

                        Row {
                            FilterChip(
                                selected = controlMode == ControlMode.DIRECT_DRAG,
                                onClick = {
                                    controlMode = ControlMode.DIRECT_DRAG
                                    repository.setControlMode(ControlMode.DIRECT_DRAG)
                                },
                                label = { Text("Touch Drag") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF),
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.testTag("control_mode_drag")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = controlMode == ControlMode.VIRTUAL_BUTTONS,
                                onClick = {
                                    controlMode = ControlMode.VIRTUAL_BUTTONS
                                    repository.setControlMode(ControlMode.VIRTUAL_BUTTONS)
                                },
                                label = { Text("D-Pad") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF),
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.testTag("control_mode_arrows")
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0x22FFFFFF))

                    // Auto-Fire Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto-Fire Blasters",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (autoFire) "Continuous plasma bursts" else "Tap screen to shoot",
                                color = Color(0xFF90A4AE),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoFire,
                            onCheckedChange = {
                                autoFire = it
                                repository.setAutoFireEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00E5FF)
                            ),
                            modifier = Modifier.testTag("auto_fire_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0x22FFFFFF))

                    // Sound & Haptics Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Audio FX",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    soundEnabled = it
                                    repository.setSoundEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00E5FF)
                                ),
                                modifier = Modifier.testTag("sound_switch")
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Haptics",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = hapticsEnabled,
                                onCheckedChange = {
                                    hapticsEnabled = it
                                    repository.setHapticsEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00E5FF)
                                ),
                                modifier = Modifier.testTag("haptics_switch")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // HOW TO PLAY BUTTON
            TextButton(
                onClick = { showHowToPlay = !showHowToPlay },
                modifier = Modifier.testTag("how_to_play_button")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = Color(0xFF90CAF9),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showHowToPlay) "Hide Flight Manual" else "Flight Manual & Power-Ups",
                    color = Color(0xFF90CAF9),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            if (showHowToPlay) {
                Surface(
                    color = Color(0x55111B38),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MISSION OBJECTIVES:",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "• Blast asteroids before they impact your ship.\n" +
                                    "• Large asteroids fragment into faster medium rocks!\n" +
                                    "• Collect power-ups: 🛡 Shield, ⚡ Twin Blaster, 💣 EMP Bomb, ❤️ Hull Repair.\n" +
                                    "• Level up every wave and brace for Dreadnought Boss battles!",
                            color = Color(0xFFECEFF1),
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
