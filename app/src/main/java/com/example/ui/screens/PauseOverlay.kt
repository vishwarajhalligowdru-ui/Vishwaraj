package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ControlMode
import com.example.data.HighScoreRepository

@Composable
fun PauseOverlay(
    repository: HighScoreRepository,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    var controlMode by remember { mutableStateOf(repository.getControlMode()) }
    var soundEnabled by remember { mutableStateOf(repository.isSoundEnabled()) }
    var hapticsEnabled by remember { mutableStateOf(repository.isHapticsEnabled()) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD9060A18))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF131A30),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MISSION PAUSED",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Steering Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Controls", color = Color.White, fontSize = 14.sp)
                    Row {
                        FilterChip(
                            selected = controlMode == ControlMode.DIRECT_DRAG,
                            onClick = {
                                controlMode = ControlMode.DIRECT_DRAG
                                repository.setControlMode(ControlMode.DIRECT_DRAG)
                            },
                            label = { Text("Drag") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF),
                                selectedLabelColor = Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
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
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sound toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sound Effects", color = Color.White, fontSize = 14.sp)
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = {
                            soundEnabled = it
                            repository.setSoundEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // RESUME
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("resume_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color.Black
                    )
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "RESUME MISSION", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RESTART
                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pause_restart_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFFFFF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "RESTART MISSION", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // MAIN MENU
                TextButton(
                    onClick = onMainMenu,
                    modifier = Modifier.testTag("pause_menu_button")
                ) {
                    Text(text = "Exit to Main Menu", color = Color(0xFF90A4AE), fontSize = 14.sp)
                }
            }
        }
    }
}
