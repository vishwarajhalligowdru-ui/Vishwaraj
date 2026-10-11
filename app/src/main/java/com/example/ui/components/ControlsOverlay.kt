package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ControlMode
import com.example.game.GameEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ControlsOverlay(
    engine: GameEngine,
    controlMode: ControlMode,
    autoFire: Boolean,
    modifier: Modifier = Modifier
) {
    if (controlMode != ControlMode.VIRTUAL_BUTTONS && autoFire) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        // D-PAD ARROW BUTTONS (BOTTOM-LEFT)
        if (controlMode == ControlMode.VIRTUAL_BUTTONS) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(160.dp)
                    .background(Color(0x3310142C), RoundedCornerShape(24.dp))
                    .padding(8.dp)
            ) {
                // UP
                ArrowButton(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("arrow_up_button"),
                    icon = Icons.Default.KeyboardArrowUp,
                    onHeld = { engine.movePlayerDelta(0f, -14f) }
                )
                // DOWN
                ArrowButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .testTag("arrow_down_button"),
                    icon = Icons.Default.KeyboardArrowDown,
                    onHeld = { engine.movePlayerDelta(0f, 14f) }
                )
                // LEFT
                ArrowButton(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .testTag("arrow_left_button"),
                    icon = Icons.Default.KeyboardArrowLeft,
                    onHeld = { engine.movePlayerDelta(-14f, 0f) }
                )
                // RIGHT
                ArrowButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .testTag("arrow_right_button"),
                    icon = Icons.Default.KeyboardArrowRight,
                    onHeld = { engine.movePlayerDelta(14f, 0f) }
                )
            }
        }

        // FIRE BUTTON (BOTTOM-RIGHT, slightly to the left of the bomb button)
        if (!autoFire) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 84.dp, bottom = 6.dp)
                    .size(64.dp)
                    .testTag("fire_laser_button")
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                engine.triggerPlayerFire(force = true)
                            }
                        )
                    },
                shape = CircleShape,
                color = Color(0xFF00E5FF),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Fire Blaster",
                        tint = Color(0xFF003740),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArrowButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onHeld: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            while (isActive) {
                onHeld()
                delay(30L)
            }
        }
    }

    Surface(
        modifier = modifier
            .size(46.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = CircleShape,
        color = if (isPressed) Color(0xFF00E5FF) else Color(0x66293241),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x8800E5FF))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPressed) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
