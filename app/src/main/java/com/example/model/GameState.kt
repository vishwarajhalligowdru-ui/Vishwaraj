package com.example.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class ScreenState {
    TITLE,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class WeaponType(val displayName: String, val fireRateMs: Long) {
    SINGLE("Single Blaster", 220L),
    DUAL("Twin Plasma", 180L),
    TRIPLE("Spread Blaster", 160L),
    BEAM("Overcharged Cannon", 120L)
}

enum class PowerUpType(val label: String, val color: Color, val symbol: String) {
    SHIELD("Shield Recharge", Color(0xFF00E5FF), "🛡"),
    WEAPON_UPGRADE("Blaster Upgrade", Color(0xFFFFD600), "⚡"),
    BOMB("EMP Bomb", Color(0xFFFF3D00), "💣"),
    HEAL("Hull Repair", Color(0xFF00E676), "❤️"),
    SCORE_BOOST("500 Bonus PTS", Color(0xFFE040FB), "⭐")
}

data class PlayerShip(
    var x: Float = 0f,
    var y: Float = 0f,
    val width: Float = 60f,
    val height: Float = 70f,
    var health: Int = 100,
    val maxHealth: Int = 100,
    var lives: Int = 3,
    var shieldActive: Boolean = false,
    var shieldTimer: Float = 0f,
    var weapon: WeaponType = WeaponType.SINGLE,
    var weaponTimer: Float = 0f,
    var bombs: Int = 2,
    var invulnerableTimer: Float = 0f
)

data class Laser(
    val id: Long = Random.nextLong(),
    var x: Float,
    var y: Float,
    val vx: Float = 0f,
    val vy: Float = -22f,
    val radius: Float = 4f,
    val color: Color = Color(0xFF00FFFF),
    val damage: Int = 1,
    val isEnemy: Boolean = false
)

enum class AsteroidSize(val radius: Float, val baseHp: Int, val scoreValue: Int) {
    SMALL(22f, 1, 100),
    MEDIUM(38f, 2, 250),
    LARGE(58f, 4, 500)
}

data class Asteroid(
    val id: Long = Random.nextLong(),
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: AsteroidSize,
    var hp: Int = size.baseHp,
    val maxHp: Int = size.baseHp,
    var rotation: Float = 0f,
    val rotationSpeed: Float = (Random.nextFloat() - 0.5f) * 4f,
    val vertexOffsets: List<Float>,
    val color: Color = Color(0xFFA5A5A5),
    val isCrystal: Boolean = false
)

data class EnemyShip(
    val id: Long = Random.nextLong(),
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var hp: Int,
    val maxHp: Int,
    var shootCooldown: Float,
    val isBoss: Boolean = false,
    val bossPhase: Int = 1
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1f,
    val decay: Float = 0.035f,
    val size: Float = 4f,
    val color: Color
)

data class PowerUp(
    val id: Long = Random.nextLong(),
    var x: Float,
    var y: Float,
    val vy: Float = 3.5f,
    val type: PowerUpType,
    var pulse: Float = 0f
)

data class FloatingText(
    val id: Long = Random.nextLong(),
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var alpha: Float = 1f,
    var vy: Float = -2f
)

data class Star(
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

data class GameStats(
    val score: Int = 0,
    val highScore: Int = 0,
    val level: Int = 1,
    val asteroidsDestroyed: Int = 0,
    val bossesDefeated: Int = 0,
    val shotsFired: Int = 0,
    val shotsHit: Int = 0,
    val maxCombo: Int = 0,
    val playTimeSeconds: Int = 0
) {
    val accuracy: Int
        get() = if (shotsFired > 0) ((shotsHit.toFloat() / shotsFired) * 100).toInt().coerceAtMost(100) else 0
}
