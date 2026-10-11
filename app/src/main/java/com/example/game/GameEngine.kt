package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundManager
import com.example.data.HighScoreRepository
import com.example.model.*
import kotlin.math.*
import kotlin.random.Random

class GameEngine(
    private val soundManager: SoundManager,
    private val repository: HighScoreRepository
) {
    var screenWidth: Float = 1080f
        private set
    var screenHeight: Float = 1920f
        private set

    var screenState: ScreenState = ScreenState.TITLE
    val player = PlayerShip()
    val lasers = mutableListOf<Laser>()
    val enemyLasers = mutableListOf<Laser>()
    val asteroids = mutableListOf<Asteroid>()
    val enemies = mutableListOf<EnemyShip>()
    val particles = mutableListOf<Particle>()
    val powerUps = mutableListOf<PowerUp>()
    val floatingTexts = mutableListOf<FloatingText>()
    val stars = mutableListOf<Star>()

    var stats = GameStats()
        private set

    var screenShake: Float = 0f
    var comboCount: Int = 0
    var comboTimer: Float = 0f
    var levelBannerTimer: Float = 0f
    var isNewHighScore: Boolean = false

    private var lastShotTime: Long = 0L
    private var asteroidSpawnTimer: Float = 0f
    private var enemySpawnTimer: Float = 0f
    private var levelWaveKills: Int = 0
    private var waveKillTarget: Int = 12

    fun setDimensions(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return
        val firstInit = screenWidth != w || screenHeight != h
        screenWidth = w
        screenHeight = h

        if (firstInit || stars.isEmpty()) {
            initStars()
            if (player.x == 0f && player.y == 0f) {
                resetPlayerPosition()
            }
        }
    }

    private fun initStars() {
        stars.clear()
        for (i in 0 until 80) {
            stars.add(
                Star(
                    x = Random.nextFloat() * screenWidth,
                    y = Random.nextFloat() * screenHeight,
                    speed = Random.nextFloat() * 2.5f + 0.8f,
                    size = Random.nextFloat() * 2.5f + 1f,
                    alpha = Random.nextFloat() * 0.7f + 0.3f
                )
            )
        }
    }

    fun startNewGame() {
        val currentHigh = repository.getHighScore()
        stats = GameStats(highScore = currentHigh)
        player.x = screenWidth / 2f
        player.y = screenHeight - 200f
        player.health = player.maxHealth
        player.lives = 3
        player.bombs = 2
        player.weapon = WeaponType.SINGLE
        player.shieldActive = false
        player.invulnerableTimer = 2.5f

        lasers.clear()
        enemyLasers.clear()
        asteroids.clear()
        enemies.clear()
        particles.clear()
        powerUps.clear()
        floatingTexts.clear()

        comboCount = 0
        comboTimer = 0f
        levelWaveKills = 0
        waveKillTarget = 12
        levelBannerTimer = 2.5f
        isNewHighScore = false

        screenState = ScreenState.PLAYING
        soundManager.playLevelUp()
    }

    fun movePlayerDirect(targetX: Float, targetY: Float) {
        if (screenState != ScreenState.PLAYING) return
        val halfW = player.width / 2f
        val halfH = player.height / 2f
        player.x = targetX.coerceIn(halfW, screenWidth - halfW)
        player.y = targetY.coerceIn(halfH, screenHeight - halfH - 60f)
    }

    fun movePlayerDelta(dx: Float, dy: Float) {
        if (screenState != ScreenState.PLAYING) return
        val halfW = player.width / 2f
        val halfH = player.height / 2f
        player.x = (player.x + dx).coerceIn(halfW, screenWidth - halfW)
        player.y = (player.y + dy).coerceIn(halfH, screenHeight - halfH - 60f)
    }

    fun triggerPlayerFire(force: Boolean = false) {
        if (screenState != ScreenState.PLAYING) return
        val now = System.currentTimeMillis()
        val cooldown = player.weapon.fireRateMs
        if (!force && now - lastShotTime < cooldown) return
        lastShotTime = now

        stats = stats.copy(shotsFired = stats.shotsFired + 1)
        when (player.weapon) {
            WeaponType.SINGLE -> {
                lasers.add(Laser(x = player.x, y = player.y - player.height / 2f, vx = 0f, vy = -26f, color = Color(0xFF00E5FF)))
                soundManager.playLaser(false)
            }
            WeaponType.DUAL -> {
                val offset = 18f
                lasers.add(Laser(x = player.x - offset, y = player.y - 15f, vx = 0f, vy = -28f, color = Color(0xFFFFD600)))
                lasers.add(Laser(x = player.x + offset, y = player.y - 15f, vx = 0f, vy = -28f, color = Color(0xFFFFD600)))
                soundManager.playLaser(true)
            }
            WeaponType.TRIPLE -> {
                lasers.add(Laser(x = player.x, y = player.y - player.height / 2f, vx = 0f, vy = -28f, color = Color(0xFF00E676)))
                lasers.add(Laser(x = player.x - 14f, y = player.y - 10f, vx = -4.5f, vy = -26f, color = Color(0xFF00E676)))
                lasers.add(Laser(x = player.x + 14f, y = player.y - 10f, vx = 4.5f, vy = -26f, color = Color(0xFF00E676)))
                soundManager.playLaser(true)
            }
            WeaponType.BEAM -> {
                lasers.add(Laser(x = player.x - 12f, y = player.y - player.height / 2f, vx = -1.5f, vy = -32f, radius = 5f, damage = 2, color = Color(0xFFFF1744)))
                lasers.add(Laser(x = player.x + 12f, y = player.y - player.height / 2f, vx = 1.5f, vy = -32f, radius = 5f, damage = 2, color = Color(0xFFFF1744)))
                soundManager.playLaser(true)
            }
        }
    }

    fun triggerBomb() {
        if (screenState != ScreenState.PLAYING || player.bombs <= 0) return
        player.bombs--
        screenShake = 24f
        soundManager.playBomb()

        // Destroy all asteroids on screen
        var bombScore = 0
        asteroids.forEach { ast ->
            bombScore += ast.size.scoreValue
            createExplosion(ast.x, ast.y, if (ast.size == AsteroidSize.LARGE) 25 else 15, Color(0xFFFF9100))
        }
        stats = stats.copy(
            score = stats.score + bombScore,
            asteroidsDestroyed = stats.asteroidsDestroyed + asteroids.size
        )
        asteroids.clear()
        enemyLasers.clear()

        // Damage enemies
        enemies.forEach { enemy ->
            enemy.hp -= 15
            createExplosion(enemy.x, enemy.y, 20, Color(0xFFFF3D00))
        }
        enemies.removeAll { it.hp <= 0 }

        addFloatingText(screenWidth / 2f, screenHeight / 2f, "EMP SHOCKWAVE!", Color(0xFFFF3D00))
    }

    fun update(dt: Float) {
        // Update Starfield
        stars.forEach { star ->
            star.y += star.speed * (if (screenState == ScreenState.PLAYING) 1.2f else 0.5f)
            if (star.y > screenHeight) {
                star.y = 0f
                star.x = Random.nextFloat() * screenWidth
            }
        }

        if (screenState != ScreenState.PLAYING) {
            updateVisualEffects(dt)
            return
        }

        // Screen Shake decay
        if (screenShake > 0f) {
            screenShake = (screenShake - dt * 25f).coerceAtLeast(0f)
        }

        // Combo decay
        if (comboCount > 0) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                comboCount = 0
            }
        }

        // Level banner timer
        if (levelBannerTimer > 0f) {
            levelBannerTimer -= dt
        }

        // Player Timers
        if (player.invulnerableTimer > 0f) {
            player.invulnerableTimer -= dt
        }
        if (player.shieldActive) {
            player.shieldTimer -= dt
            if (player.shieldTimer <= 0f) {
                player.shieldActive = false
                addFloatingText(player.x, player.y - 40f, "SHIELD EXPIRED", Color(0xFFB0BEC5))
            }
        }
        if (player.weapon != WeaponType.SINGLE) {
            player.weaponTimer -= dt
            if (player.weaponTimer <= 0f) {
                player.weapon = WeaponType.SINGLE
                addFloatingText(player.x, player.y - 40f, "BLASTER STANDARD", Color(0xFFB0BEC5))
            }
        }

        // Update Lasers
        val laserIter = lasers.iterator()
        while (laserIter.hasNext()) {
            val laser = laserIter.next()
            laser.x += laser.vx
            laser.y += laser.vy
            if (laser.y < -50f || laser.x < -50f || laser.x > screenWidth + 50f) {
                laserIter.remove()
            }
        }

        // Update Enemy Lasers
        val eLaserIter = enemyLasers.iterator()
        while (eLaserIter.hasNext()) {
            val laser = eLaserIter.next()
            laser.x += laser.vx
            laser.y += laser.vy

            // Collision with player
            val dist = hypot(laser.x - player.x, laser.y - player.y)
            if (dist < player.width / 2f + laser.radius && player.invulnerableTimer <= 0f) {
                eLaserIter.remove()
                damagePlayer(25)
                createExplosion(player.x, player.y, 10, Color(0xFFFF1744))
                continue
            }

            if (laser.y > screenHeight + 50f || laser.x < -50f || laser.x > screenWidth + 50f) {
                eLaserIter.remove()
            }
        }

        // Spawn Asteroids
        asteroidSpawnTimer += dt
        val spawnInterval = (1.5f - (stats.level * 0.08f)).coerceAtLeast(0.6f)
        if (asteroidSpawnTimer >= spawnInterval) {
            asteroidSpawnTimer = 0f
            spawnAsteroid()
        }

        // Spawn Enemies at Level 2+
        if (stats.level >= 2) {
            enemySpawnTimer += dt
            val enemyInterval = (6.0f - (stats.level * 0.4f)).coerceAtLeast(2.5f)
            if (enemySpawnTimer >= enemyInterval && enemies.size < 3) {
                enemySpawnTimer = 0f
                spawnEnemy()
            }
        }

        // Update Asteroids
        val astIter = asteroids.iterator()
        while (astIter.hasNext()) {
            val ast = astIter.next()
            ast.x += ast.vx
            ast.y += ast.vy
            ast.rotation += ast.rotationSpeed

            // Collision with Lasers
            var destroyed = false
            for (laser in lasers.toList()) {
                val dist = hypot(laser.x - ast.x, laser.y - ast.y)
                if (dist < ast.size.radius + laser.radius) {
                    lasers.remove(laser)
                    ast.hp -= laser.damage
                    stats = stats.copy(shotsHit = stats.shotsHit + 1)
                    createSparks(laser.x, laser.y, 5, laser.color)

                    if (ast.hp <= 0) {
                        destroyed = true
                        astIter.remove()
                        onAsteroidDestroyed(ast)
                        break
                    }
                }
            }

            if (!destroyed) {
                // Collision with Player
                val distToPlayer = hypot(ast.x - player.x, ast.y - player.y)
                if (distToPlayer < ast.size.radius + player.width / 2.2f && player.invulnerableTimer <= 0f) {
                    astIter.remove()
                    createExplosion(ast.x, ast.y, 20, Color(0xFFFF7043))
                    damagePlayer(if (ast.size == AsteroidSize.LARGE) 45 else 25)
                    soundManager.playExplosion(large = ast.size == AsteroidSize.LARGE)
                    continue
                }

                // Remove off-screen
                if (ast.y > screenHeight + 80f) {
                    astIter.remove()
                }
            }
        }

        // Update Enemies
        val enemyIter = enemies.iterator()
        while (enemyIter.hasNext()) {
            val enemy = enemyIter.next()
            enemy.x += enemy.vx
            enemy.y += enemy.vy

            // Bounce off sides
            if (enemy.x < 50f || enemy.x > screenWidth - 50f) {
                enemy.vx = -enemy.vx
            }

            // Enemy Shooting
            enemy.shootCooldown -= dt
            if (enemy.shootCooldown <= 0f) {
                enemy.shootCooldown = if (enemy.isBoss) 1.2f else 2.2f
                enemyLasers.add(Laser(x = enemy.x, y = enemy.y + 30f, vx = 0f, vy = 11f, radius = 5f, color = Color(0xFFFF1744), isEnemy = true))
                if (enemy.isBoss) {
                    enemyLasers.add(Laser(x = enemy.x - 20f, y = enemy.y + 25f, vx = -3f, vy = 10f, radius = 5f, color = Color(0xFFFF5252), isEnemy = true))
                    enemyLasers.add(Laser(x = enemy.x + 20f, y = enemy.y + 25f, vx = 3f, vy = 10f, radius = 5f, color = Color(0xFFFF5252), isEnemy = true))
                }
            }

            // Laser vs Enemy
            var enemyKilled = false
            for (laser in lasers.toList()) {
                val dist = hypot(laser.x - enemy.x, laser.y - enemy.y)
                val targetRadius = if (enemy.isBoss) 70f else 35f
                if (dist < targetRadius) {
                    lasers.remove(laser)
                    enemy.hp -= laser.damage
                    stats = stats.copy(shotsHit = stats.shotsHit + 1)
                    createSparks(laser.x, laser.y, 6, Color(0xFFFF5252))

                    if (enemy.hp <= 0) {
                        enemyKilled = true
                        enemyIter.remove()
                        onEnemyDestroyed(enemy)
                        break
                    }
                }
            }

            if (!enemyKilled && enemy.y > screenHeight + 60f) {
                enemyIter.remove()
            }
        }

        // Update PowerUps
        val pIter = powerUps.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.y += p.vy
            p.pulse += dt * 6f

            val dist = hypot(p.x - player.x, p.y - player.y)
            if (dist < player.width / 2f + 25f) {
                pIter.remove()
                collectPowerUp(p)
                continue
            }

            if (p.y > screenHeight + 50f) {
                pIter.remove()
            }
        }

        updateVisualEffects(dt)
    }

    private fun updateVisualEffects(dt: Float) {
        // Update Particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx
            p.y += p.vy
            p.alpha -= p.decay
            if (p.alpha <= 0f) {
                pIter.remove()
            }
        }

        // Update Floating Texts
        val ftIter = floatingTexts.iterator()
        while (ftIter.hasNext()) {
            val ft = ftIter.next()
            ft.y += ft.vy
            ft.alpha -= 0.02f
            if (ft.alpha <= 0f) {
                ftIter.remove()
            }
        }
    }

    private fun spawnAsteroid() {
        val sizeRoll = Random.nextFloat()
        val size = when {
            sizeRoll < 0.28f -> AsteroidSize.LARGE
            sizeRoll < 0.65f -> AsteroidSize.MEDIUM
            else -> AsteroidSize.SMALL
        }

        val x = Random.nextFloat() * (screenWidth - 100f) + 50f
        val y = -size.radius * 2f
        val baseSpeed = 2.2f + (stats.level * 0.4f)
        val vy = baseSpeed * (Random.nextFloat() * 0.6f + 0.8f)
        val vx = (Random.nextFloat() - 0.5f) * 2.2f

        val isCrystal = Random.nextFloat() < 0.12f

        // Generate irregular polygon offsets for craggy shape
        val numVertices = 10
        val offsets = List(numVertices) {
            Random.nextFloat() * 0.35f + 0.82f
        }

        asteroids.add(
            Asteroid(
                x = x,
                y = y,
                vx = vx,
                vy = vy,
                size = size,
                vertexOffsets = offsets,
                color = if (isCrystal) Color(0xFF00E5FF) else Color(0xFF8D6E63),
                isCrystal = isCrystal
            )
        )
    }

    private fun spawnEnemy() {
        val isBoss = (stats.level % 3 == 0) && enemies.none { it.isBoss }
        val hp = if (isBoss) 25 + stats.level * 10 else 4 + stats.level
        val enemy = EnemyShip(
            x = Random.nextFloat() * (screenWidth - 140f) + 70f,
            y = -80f,
            vx = if (Random.nextBoolean()) 2.5f else -2.5f,
            vy = if (isBoss) 0.6f else 2.0f,
            hp = hp,
            maxHp = hp,
            shootCooldown = 1.5f,
            isBoss = isBoss
        )
        enemies.add(enemy)
        if (isBoss) {
            addFloatingText(screenWidth / 2f, 220f, "WARNING: DREADNOUGHT AHEAD!", Color(0xFFFF1744))
        }
    }

    private fun onAsteroidDestroyed(ast: Asteroid) {
        soundManager.playExplosion(large = ast.size == AsteroidSize.LARGE)
        createExplosion(ast.x, ast.y, if (ast.size == AsteroidSize.LARGE) 25 else 14, ast.color)

        // Increment Combos
        comboCount++
        comboTimer = 2.8f
        val comboMultiplier = (1 + comboCount / 4).coerceAtMost(5)
        val gainedScore = ast.size.scoreValue * comboMultiplier

        stats = stats.copy(
            score = stats.score + gainedScore,
            asteroidsDestroyed = stats.asteroidsDestroyed + 1,
            maxCombo = maxOf(stats.maxCombo, comboCount)
        )

        addFloatingText(
            ast.x,
            ast.y,
            "+$gainedScore" + if (comboMultiplier > 1) " (${comboMultiplier}x)" else "",
            if (ast.isCrystal) Color(0xFF00E5FF) else Color(0xFFFFD54F)
        )

        // Split Asteroids (Large splits into 2 Medium, Medium splits into 2 Small)
        if (ast.size == AsteroidSize.LARGE) {
            spawnChildAsteroid(ast.x - 20f, ast.y, -2.5f, ast.vy * 1.2f, AsteroidSize.MEDIUM)
            spawnChildAsteroid(ast.x + 20f, ast.y, 2.5f, ast.vy * 1.2f, AsteroidSize.MEDIUM)
        } else if (ast.size == AsteroidSize.MEDIUM) {
            spawnChildAsteroid(ast.x - 15f, ast.y, -3.2f, ast.vy * 1.3f, AsteroidSize.SMALL)
            spawnChildAsteroid(ast.x + 15f, ast.y, 3.2f, ast.vy * 1.3f, AsteroidSize.SMALL)
        }

        // Power-Up Drops (Crystal has 100% drop, others have 12% drop)
        if (ast.isCrystal || Random.nextFloat() < 0.12f) {
            dropPowerUp(ast.x, ast.y)
        }

        // Check wave progression
        levelWaveKills++
        if (levelWaveKills >= waveKillTarget) {
            levelUp()
        }
    }

    private fun spawnChildAsteroid(x: Float, y: Float, vx: Float, vy: Float, size: AsteroidSize) {
        val offsets = List(10) { Random.nextFloat() * 0.3f + 0.85f }
        asteroids.add(
            Asteroid(
                x = x.coerceIn(50f, screenWidth - 50f),
                y = y,
                vx = vx,
                vy = vy,
                size = size,
                vertexOffsets = offsets,
                color = Color(0xFFA1887F)
            )
        )
    }

    private fun onEnemyDestroyed(enemy: EnemyShip) {
        soundManager.playExplosion(large = enemy.isBoss)
        createExplosion(enemy.x, enemy.y, if (enemy.isBoss) 45 else 20, Color(0xFFFF5252))

        val bonus = if (enemy.isBoss) 3000 else 800
        stats = stats.copy(
            score = stats.score + bonus,
            bossesDefeated = stats.bossesDefeated + (if (enemy.isBoss) 1 else 0)
        )

        addFloatingText(enemy.x, enemy.y, "+$bonus!", Color(0xFFFF5252))
        dropPowerUp(enemy.x, enemy.y)
        if (enemy.isBoss) {
            dropPowerUp(enemy.x - 30f, enemy.y)
            screenShake = 22f
        }
    }

    private fun dropPowerUp(x: Float, y: Float) {
        val types = PowerUpType.values()
        val picked = types[Random.nextInt(types.size)]
        powerUps.add(PowerUp(x = x, y = y, type = picked))
    }

    private fun collectPowerUp(p: PowerUp) {
        soundManager.playPowerup()
        when (p.type) {
            PowerUpType.SHIELD -> {
                player.shieldActive = true
                player.shieldTimer = 14f
                addFloatingText(player.x, player.y - 40f, "SHIELDS ONLINE!", p.type.color)
            }
            PowerUpType.WEAPON_UPGRADE -> {
                player.weapon = when (player.weapon) {
                    WeaponType.SINGLE -> WeaponType.DUAL
                    WeaponType.DUAL -> WeaponType.TRIPLE
                    WeaponType.TRIPLE -> WeaponType.BEAM
                    WeaponType.BEAM -> WeaponType.BEAM
                }
                player.weaponTimer = 18f
                addFloatingText(player.x, player.y - 40f, player.weapon.displayName.uppercase(), p.type.color)
            }
            PowerUpType.BOMB -> {
                player.bombs = (player.bombs + 1).coerceAtMost(5)
                addFloatingText(player.x, player.y - 40f, "+1 EMP BOMB", p.type.color)
            }
            PowerUpType.HEAL -> {
                player.health = (player.health + 40).coerceAtMost(player.maxHealth)
                addFloatingText(player.x, player.y - 40f, "+40 REPAIRS", p.type.color)
            }
            PowerUpType.SCORE_BOOST -> {
                stats = stats.copy(score = stats.score + 500)
                addFloatingText(player.x, player.y - 40f, "+500 BONUS", p.type.color)
            }
        }
    }

    private fun damagePlayer(amount: Int) {
        if (player.invulnerableTimer > 0f) return

        if (player.shieldActive) {
            soundManager.playHit()
            addFloatingText(player.x, player.y - 40f, "SHIELD DEFLECTED!", Color(0xFF00E5FF))
            player.shieldTimer = (player.shieldTimer - 2.5f).coerceAtLeast(0f)
            if (player.shieldTimer <= 0f) player.shieldActive = false
            return
        }

        player.health -= amount
        screenShake = 16f
        soundManager.playHit()

        if (player.health <= 0) {
            player.lives--
            if (player.lives > 0) {
                player.health = player.maxHealth
                player.invulnerableTimer = 3.0f
                player.weapon = WeaponType.SINGLE
                addFloatingText(player.x, player.y - 40f, "SYSTEM REBOOT! LIVES: ${player.lives}", Color(0xFFFF5252))
            } else {
                triggerGameOver()
            }
        }
    }

    private fun triggerGameOver() {
        screenState = ScreenState.GAME_OVER
        soundManager.playGameOver()
        screenShake = 30f
        createExplosion(player.x, player.y, 45, Color(0xFFFF1744))

        isNewHighScore = repository.saveScore(
            score = stats.score,
            level = stats.level,
            asteroids = stats.asteroidsDestroyed
        )
        stats = stats.copy(highScore = repository.getHighScore())
    }

    private fun levelUp() {
        levelWaveKills = 0
        waveKillTarget = 12 + stats.level * 4
        stats = stats.copy(level = stats.level + 1)
        levelBannerTimer = 3.0f
        soundManager.playLevelUp()
        addFloatingText(screenWidth / 2f, screenHeight / 2f - 60f, "LEVEL ${stats.level} REACHED!", Color(0xFF00E5FF))

        // Bonus repairs on level up
        player.health = (player.health + 25).coerceAtMost(player.maxHealth)
    }

    private fun createExplosion(x: Float, y: Float, count: Int, color: Color) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 7f + 2f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    alpha = 1f,
                    decay = Random.nextFloat() * 0.03f + 0.02f,
                    size = Random.nextFloat() * 4f + 2f,
                    color = color
                )
            )
        }
    }

    private fun createSparks(x: Float, y: Float, count: Int, color: Color) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = Random.nextFloat() * 4f + 1f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    alpha = 1f,
                    decay = 0.06f,
                    size = 2.5f,
                    color = color
                )
            )
        }
    }

    private fun addFloatingText(x: Float, y: Float, text: String, color: Color) {
        floatingTexts.add(
            FloatingText(
                x = x.coerceIn(80f, screenWidth - 80f),
                y = y,
                text = text,
                color = color
            )
        )
    }

    private fun resetPlayerPosition() {
        player.x = screenWidth / 2f
        player.y = screenHeight - 200f
    }
}
