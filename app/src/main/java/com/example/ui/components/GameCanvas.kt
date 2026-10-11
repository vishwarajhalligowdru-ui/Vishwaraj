package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.ControlMode
import com.example.game.GameEngine
import com.example.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvas(
    engine: GameEngine,
    controlMode: ControlMode,
    autoFire: Boolean,
    modifier: Modifier = Modifier
) {
    var touchActive by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(controlMode, autoFire) {
                if (controlMode == ControlMode.DIRECT_DRAG) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchActive = true
                            engine.movePlayerDirect(offset.x, offset.y)
                        },
                        onDragEnd = { touchActive = false },
                        onDragCancel = { touchActive = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            engine.movePlayerDelta(dragAmount.x, dragAmount.y)
                        }
                    )
                }
            }
            .pointerInput(controlMode, autoFire) {
                detectTapGestures(
                    onPress = { offset ->
                        if (controlMode == ControlMode.DIRECT_DRAG) {
                            engine.movePlayerDirect(offset.x, offset.y)
                        }
                        if (!autoFire) {
                            engine.triggerPlayerFire(force = true)
                        }
                    }
                )
            }
    ) {
        engine.setDimensions(size.width, size.height)

        // Screen Shake offset
        val shakeOffset = if (engine.screenShake > 0f) {
            Offset(
                (Random.nextFloat() - 0.5f) * engine.screenShake * 1.2f,
                (Random.nextFloat() - 0.5f) * engine.screenShake * 1.2f
            )
        } else Offset.Zero

        // Draw Deep Space background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF030712),
                    Color(0xFF080D21),
                    Color(0xFF0D1127)
                )
            )
        )

        // Draw Starfield
        engine.stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.size,
                center = Offset(star.x + shakeOffset.x, star.y + shakeOffset.y)
            )
        }

        // Draw Power-ups
        engine.powerUps.forEach { p ->
            drawPowerUp(p, shakeOffset)
        }

        // Draw Asteroids
        engine.asteroids.forEach { ast ->
            drawAsteroid(ast, shakeOffset)
        }

        // Draw Enemies
        engine.enemies.forEach { enemy ->
            drawEnemy(enemy, shakeOffset)
        }

        // Draw Lasers
        engine.lasers.forEach { laser ->
            drawLaser(laser, shakeOffset)
        }

        // Draw Enemy Lasers
        engine.enemyLasers.forEach { laser ->
            drawLaser(laser, shakeOffset)
        }

        // Draw Player Ship (if playing or paused)
        if (engine.screenState == ScreenState.PLAYING || engine.screenState == ScreenState.PAUSED) {
            drawPlayerShip(engine.player, shakeOffset)
        }

        // Draw Particles
        engine.particles.forEach { part ->
            drawCircle(
                color = part.color.copy(alpha = part.alpha.coerceIn(0f, 1f)),
                radius = part.size,
                center = Offset(part.x + shakeOffset.x, part.y + shakeOffset.y)
            )
        }

        // Draw Floating Texts
        val nativeCanvas = drawContext.canvas.nativeCanvas
        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 34f
            isFakeBoldText = true
        }

        engine.floatingTexts.forEach { ft ->
            textPaint.color = ft.color.copy(alpha = ft.alpha.coerceIn(0f, 1f)).toArgb()
            nativeCanvas.drawText(
                ft.text,
                ft.x + shakeOffset.x,
                ft.y + shakeOffset.y,
                textPaint
            )
        }
    }
}

private fun DrawScope.drawPlayerShip(player: PlayerShip, shakeOffset: Offset) {
    // If invulnerable, flicker
    if (player.invulnerableTimer > 0f && (player.invulnerableTimer * 10).toInt() % 2 == 0) {
        return
    }

    val cx = player.x + shakeOffset.x
    val cy = player.y + shakeOffset.y
    val halfW = player.width / 2f
    val halfH = player.height / 2f

    // Animated Thruster exhaust plume
    val flicker = Random.nextFloat() * 14f + 22f
    val thrusterPath = Path().apply {
        moveTo(cx - 10f, cy + halfH - 8f)
        lineTo(cx, cy + halfH + flicker)
        lineTo(cx + 10f, cy + halfH - 8f)
        close()
    }
    drawPath(
        path = thrusterPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF9100), Color(0xFFFF3D00), Color(0x00FF3D00)),
            startY = cy + halfH - 8f,
            endY = cy + halfH + flicker
        )
    )

    // Inner core cyan flame
    val innerFlame = Path().apply {
        moveTo(cx - 5f, cy + halfH - 8f)
        lineTo(cx, cy + halfH + flicker * 0.6f)
        lineTo(cx + 5f, cy + halfH - 8f)
        close()
    }
    drawPath(path = innerFlame, color = Color(0xFF00E5FF))

    // Ship Hull Path
    val shipPath = Path().apply {
        moveTo(cx, cy - halfH) // Nose
        lineTo(cx + halfW * 0.35f, cy - halfH * 0.25f) // Upper body
        lineTo(cx + halfW, cy + halfH * 0.55f) // Wingtip right
        lineTo(cx + halfW * 0.75f, cy + halfH * 0.85f) // Wing back
        lineTo(cx + halfW * 0.28f, cy + halfH * 0.65f) // Engine pod right
        lineTo(cx, cy + halfH * 0.45f) // Center exhaust
        lineTo(cx - halfW * 0.28f, cy + halfH * 0.65f) // Engine pod left
        lineTo(cx - halfW * 0.75f, cy + halfH * 0.85f) // Wing back
        lineTo(cx - halfW, cy + halfH * 0.55f) // Wingtip left
        lineTo(cx - halfW * 0.35f, cy - halfH * 0.25f) // Upper body
        close()
    }

    // Ship hull gradient
    drawPath(
        path = shipPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE0E0E0), Color(0xFF64B5F6), Color(0xFF1976D2)),
            startY = cy - halfH,
            endY = cy + halfH
        )
    )
    drawPath(
        path = shipPath,
        color = Color(0xFF00E5FF),
        style = Stroke(width = 2.5f)
    )

    // Wing cannons
    drawCircle(color = Color(0xFFFFD600), radius = 3.5f, center = Offset(cx - halfW + 4f, cy + halfH * 0.55f))
    drawCircle(color = Color(0xFFFFD600), radius = 3.5f, center = Offset(cx + halfW - 4f, cy + halfH * 0.55f))

    // Cockpit canopy glow
    val cockpitPath = Path().apply {
        moveTo(cx, cy - halfH * 0.6f)
        lineTo(cx + 9f, cy - 5f)
        lineTo(cx, cy + 8f)
        lineTo(cx - 9f, cy - 5f)
        close()
    }
    drawPath(path = cockpitPath, color = Color(0xFF00E5FF))
    drawPath(path = cockpitPath, color = Color.White, style = Stroke(width = 1.5f))

    // Energy Shield Bubble if active
    if (player.shieldActive) {
        val shieldRadius = player.width * 0.85f
        drawCircle(
            color = Color(0x3300E5FF),
            radius = shieldRadius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = shieldRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 3f)
        )
    }
}

private fun DrawScope.drawAsteroid(ast: Asteroid, shakeOffset: Offset) {
    val cx = ast.x + shakeOffset.x
    val cy = ast.y + shakeOffset.y
    val r = ast.size.radius

    rotate(ast.rotation, pivot = Offset(cx, cy)) {
        val path = Path()
        val numVertices = ast.vertexOffsets.size
        for (i in 0 until numVertices) {
            val angle = (i.toFloat() / numVertices) * (2f * PI.toFloat())
            val currentR = r * ast.vertexOffsets[i]
            val vx = cx + cos(angle) * currentR
            val vy = cy + sin(angle) * currentR
            if (i == 0) path.moveTo(vx, vy) else path.lineTo(vx, vy)
        }
        path.close()

        val fillBrush = if (ast.isCrystal) {
            Brush.radialGradient(
                colors = listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF006064)),
                center = Offset(cx - r * 0.3f, cy - r * 0.3f),
                radius = r * 1.2f
            )
        } else {
            Brush.radialGradient(
                colors = listOf(Color(0xFFBCAAA4), ast.color, Color(0xFF3E2723)),
                center = Offset(cx - r * 0.3f, cy - r * 0.3f),
                radius = r * 1.3f
            )
        }

        drawPath(path = path, brush = fillBrush)
        drawPath(
            path = path,
            color = if (ast.isCrystal) Color(0xFF80DEEA) else Color(0xFF5D4037),
            style = Stroke(width = 2f)
        )

        // Craters on non-crystal
        if (!ast.isCrystal) {
            drawCircle(color = Color(0x332E1C14), radius = r * 0.22f, center = Offset(cx - r * 0.25f, cy - r * 0.2f))
            drawCircle(color = Color(0x332E1C14), radius = r * 0.16f, center = Offset(cx + r * 0.3f, cy + r * 0.2f))
        }

        // Damaged health indicator for Large asteroids
        if (ast.size == AsteroidSize.LARGE && ast.hp < ast.maxHp) {
            val healthPercent = ast.hp.toFloat() / ast.maxHp
            drawRoundRect(
                color = Color(0x88000000),
                topLeft = Offset(cx - 24f, cy - r - 16f),
                size = Size(48f, 6f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color(0xFFFF5252),
                topLeft = Offset(cx - 24f, cy - r - 16f),
                size = Size(48f * healthPercent, 6f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}

private fun DrawScope.drawEnemy(enemy: EnemyShip, shakeOffset: Offset) {
    val cx = enemy.x + shakeOffset.x
    val cy = enemy.y + shakeOffset.y

    if (enemy.isBoss) {
        // Boss Dreadnought
        val w = 140f
        val h = 100f
        val bossPath = Path().apply {
            moveTo(cx, cy + h * 0.45f) // Front cannon
            lineTo(cx + w * 0.35f, cy + h * 0.2f)
            lineTo(cx + w * 0.5f, cy - h * 0.25f) // Wing right
            lineTo(cx + w * 0.3f, cy - h * 0.5f) // Top right
            lineTo(cx - w * 0.3f, cy - h * 0.5f) // Top left
            lineTo(cx - w * 0.5f, cy - h * 0.25f) // Wing left
            lineTo(cx - w * 0.35f, cy + h * 0.2f)
            close()
        }
        drawPath(
            path = bossPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF263238), Color(0xFFB71C1C), Color(0xFFD32F2F)),
                startY = cy - h * 0.5f,
                endY = cy + h * 0.45f
            )
        )
        drawPath(path = bossPath, color = Color(0xFFFF1744), style = Stroke(width = 3f))

        // Boss Core
        drawCircle(color = Color(0xFFFFEA00), radius = 16f, center = Offset(cx, cy))
        drawCircle(color = Color(0xFFFF1744), radius = 8f, center = Offset(cx, cy))

        // Boss Health Bar
        val hpPct = enemy.hp.toFloat() / enemy.maxHp
        drawRoundRect(
            color = Color(0xCC000000),
            topLeft = Offset(cx - 60f, cy - h * 0.65f),
            size = Size(120f, 10f),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawRoundRect(
            color = Color(0xFFFF1744),
            topLeft = Offset(cx - 60f, cy - h * 0.65f),
            size = Size(120f * hpPct, 10f),
            cornerRadius = CornerRadius(5f, 5f)
        )
    } else {
        // Fast Alien Raider
        val w = 48f
        val h = 42f
        val alienPath = Path().apply {
            moveTo(cx, cy + h * 0.5f) // Nose down
            lineTo(cx + w * 0.5f, cy - h * 0.3f)
            lineTo(cx + w * 0.2f, cy - h * 0.5f)
            lineTo(cx, cy - h * 0.2f)
            lineTo(cx - w * 0.2f, cy - h * 0.5f)
            lineTo(cx - w * 0.5f, cy - h * 0.3f)
            close()
        }
        drawPath(
            path = alienPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF4A148C), Color(0xFF9C27B0), Color(0xFFE040FB)),
                startY = cy - h * 0.5f,
                endY = cy + h * 0.5f
            )
        )
        drawPath(path = alienPath, color = Color(0xFFE040FB), style = Stroke(width = 2f))
        drawCircle(color = Color(0xFFFF1744), radius = 5f, center = Offset(cx, cy + 4f))
    }
}

private fun DrawScope.drawLaser(laser: Laser, shakeOffset: Offset) {
    val lx = laser.x + shakeOffset.x
    val ly = laser.y + shakeOffset.y

    val height = if (laser.isEnemy) 20f else 28f
    val width = laser.radius * 2.2f

    // Outer glow
    drawRoundRect(
        color = laser.color.copy(alpha = 0.45f),
        topLeft = Offset(lx - width * 0.9f, ly - height / 2f),
        size = Size(width * 1.8f, height),
        cornerRadius = CornerRadius(width, width)
    )
    // Core beam
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(lx - width * 0.45f, ly - height / 2f + 2f),
        size = Size(width * 0.9f, height - 4f),
        cornerRadius = CornerRadius(width / 2f, width / 2f)
    )
}

private fun DrawScope.drawPowerUp(p: PowerUp, shakeOffset: Offset) {
    val px = p.x + shakeOffset.x
    val py = p.y + shakeOffset.y
    val r = 20f

    // Pulsing outer halo
    val pulseSize = r + sin(p.pulse) * 4f
    drawCircle(
        color = p.type.color.copy(alpha = 0.3f),
        radius = pulseSize * 1.3f,
        center = Offset(px, py)
    )
    drawCircle(
        color = p.type.color,
        radius = r,
        center = Offset(px, py),
        style = Stroke(width = 2.5f)
    )
    drawCircle(
        color = Color(0xFF1E1E2E),
        radius = r - 2f,
        center = Offset(px, py)
    )

    // Symbol text
    val nativeCanvas = drawContext.canvas.nativeCanvas
    val symPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = 24f
    }
    nativeCanvas.drawText(p.type.symbol, px, py + 8f, symPaint)
}
