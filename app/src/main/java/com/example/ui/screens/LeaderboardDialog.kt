package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.HighScoreRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LeaderboardDialog(
    repository: HighScoreRepository,
    onDismiss: () -> Unit
) {
    val entries = repository.getLeaderboard()
    val totalKills = repository.getTotalKills()
    val totalGames = repository.getTotalGames()
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF10172D),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E5FF)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HALL OF FAME",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_leaderboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFB0BEC5)
                        )
                    }
                }

                // Lifetime Stats banner
                Surface(
                    color = Color(0x3300E5FF),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "MISSIONS", color = Color(0xFF90CAF9), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "$totalGames", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ROCKS DESTROYED", color = Color(0xFF90CAF9), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = "$totalKills", color = Color(0xFFFFD54F), fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // List of runs
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded missions yet.\nLaunch a flight to establish records!",
                            color = Color(0xFF90A4AE),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(entries) { index, entry ->
                            val rankColor = when (index) {
                                0 -> Color(0xFFFFD600)
                                1 -> Color(0xFFB0BEC5)
                                2 -> Color(0xFFCD7F32)
                                else -> Color(0xFF64B5F6)
                            }

                            Surface(
                                color = Color(0x22FFFFFF),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = rankColor,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            modifier = Modifier.width(36.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "${entry.score} PTS",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "Sector ${entry.level} • ${entry.asteroidsDestroyed} Kills",
                                                color = Color(0xFFB0BEC5),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = dateFormat.format(Date(entry.timestamp)),
                                        color = Color(0xFF78909C),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = "DISMISS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
