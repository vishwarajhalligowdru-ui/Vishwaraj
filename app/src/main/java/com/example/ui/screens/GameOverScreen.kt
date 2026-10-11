package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameStats

@Composable
fun GameOverScreen(
    stats: GameStats,
    isNewHighScore: Boolean,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val buttonScale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "restartScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD9060A18))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // GAME OVER TITLE
            Text(
                text = "GAME OVER",
                color = Color(0xFFFF1744),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("game_over_title")
            )

            Text(
                text = "STARSHIP HULL COMPROMISED",
                color = Color(0xFFFF8A80),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // NEW RECORD BANNER
            if (isNewHighScore) {
                Surface(
                    color = Color(0x33FFD600),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD600)),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👑", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEW HIGH SCORE RECORD!",
                            color = Color(0xFFFFD600),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // SCORE HERO CARD
            Surface(
                color = Color(0x441A233A),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4400E5FF)),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FINAL SCORE",
                        color = Color(0xFF90CAF9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${stats.score}",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .testTag("final_score_text")
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = Color(0x33FFFFFF)
                    )

                    // STATS GRID
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "SECTOR REACHED", value = "LVL ${stats.level}")
                        StatItem(label = "ASTEROIDS SHOT", value = "${stats.asteroidsDestroyed}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "TARGET ACCURACY", value = "${stats.accuracy}%")
                        StatItem(label = "MAX COMBO", value = "${stats.maxCombo}x")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // RESTART BUTTON (PRIMARY)
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(60.dp)
                    .scale(buttonScale)
                    .testTag("restart_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF060A18)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY AGAIN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // MAIN MENU BUTTON (SECONDARY)
            OutlinedButton(
                onClick = onMainMenu,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
                    .testTag("main_menu_button"),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0x66FFFFFF)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RETURN TO BASE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color(0xFF90A4AE),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = Color(0xFFE0E0E0),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
