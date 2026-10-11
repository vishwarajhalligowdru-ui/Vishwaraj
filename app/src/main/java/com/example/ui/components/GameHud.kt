package com.example.ui.components

import androidx.compose.animation.*
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameEngine
import com.example.model.WeaponType

@Composable
fun GameHud(
    engine: GameEngine,
    onPauseClick: () -> Unit,
    onToggleSound: () -> Unit,
    soundEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val stats = engine.stats
    val player = engine.player

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // TOP BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Level Badge
                Column {
                    Text(
                        text = "SCORE",
                        color = Color(0xFF90CAF9),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${stats.score}",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("score_counter")
                    )
                }

                // Level Pill
                Surface(
                    color = Color(0x3300E5FF),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LVL ${stats.level}",
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // High Score & Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "HIGH",
                            color = Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stats.highScore.coerceAtLeast(stats.score)}",
                            color = Color(0xFFFFD54F),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x33000000), CircleShape)
                            .testTag("sound_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Sound",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onPauseClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x33000000), CircleShape)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause Game",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // HEALTH BAR & LIVES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Health Bar
                Column(modifier = Modifier.weight(1f)) {
                    val hpRatio = (player.health.toFloat() / player.maxHealth).coerceIn(0f, 1f)
                    val hpColor = when {
                        hpRatio > 0.5f -> Color(0xFF00E676)
                        hpRatio > 0.25f -> Color(0xFFFFD600)
                        else -> Color(0xFFFF1744)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x55000000))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(hpRatio)
                                .background(hpColor)
                        )
                    }
                }

                // Remaining Lives Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 0 until 3) {
                        Icon(
                            imageVector = Icons.Default.Flight,
                            contentDescription = "Life $i",
                            tint = if (i < player.lives) Color(0xFF00E5FF) else Color(0x33FFFFFF),
                            modifier = Modifier
                                .size(18.dp)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            // ACTIVE WEAPON / SHIELD BADGES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                if (player.weapon != WeaponType.SINGLE) {
                    Surface(
                        color = Color(0x44FFD600),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "⚡ ${player.weapon.displayName} (${player.weaponTimer.toInt()}s)",
                            color = Color(0xFFFFD600),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                if (player.shieldActive) {
                    Surface(
                        color = Color(0x4400E5FF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🛡 SHIELD (${player.shieldTimer.toInt()}s)",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // COMBO BANNER (MID-LEFT)
        if (engine.comboCount >= 2) {
            val comboAnim = rememberInfiniteTransition(label = "combo")
            val scale by comboAnim.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(250, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .scale(scale)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFF3D00), Color(0xFFFF9100))
                        ),
                        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${engine.comboCount}x COMBO!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }

        // LEVEL UP BANNER OVERLAY
        if (engine.levelBannerTimer > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        color = Color(0xCC0A0E27),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 28.dp, vertical = 16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "WAVE INCOMING",
                        color = Color(0xFFFFD600),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "LEVEL ${stats.level}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // BOTTOM CONTROLS: EMP BOMB BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp)
        ) {
            val hasBombs = player.bombs > 0
            val pulseTransition = rememberInfiniteTransition(label = "bombPulse")
            val bombScale by pulseTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (hasBombs) 1.08f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Surface(
                onClick = { engine.triggerBomb() },
                enabled = hasBombs,
                shape = CircleShape,
                color = if (hasBombs) Color(0xFFD50000) else Color(0x33757575),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (hasBombs) Color(0xFFFF5252) else Color(0x559E9E9E)
                ),
                modifier = Modifier
                    .size(68.dp)
                    .scale(bombScale)
                    .testTag("emp_bomb_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "💣",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "x${player.bombs}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
