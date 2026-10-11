package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050
    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    private val soundBuffers = ConcurrentHashMap<String, ShortArray>()

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        // Pre-generate procedural PCM audio clips
        audioScope.launch {
            soundBuffers["laser"] = generateLaserPcm()
            soundBuffers["laser_heavy"] = generateHeavyLaserPcm()
            soundBuffers["explosion_small"] = generateExplosionPcm(durationMs = 180, decayRate = 8f)
            soundBuffers["explosion_large"] = generateExplosionPcm(durationMs = 400, decayRate = 4f)
            soundBuffers["hit"] = generateHitPcm()
            soundBuffers["powerup"] = generatePowerupPcm()
            soundBuffers["bomb"] = generateBombPcm()
            soundBuffers["game_over"] = generateGameOverPcm()
            soundBuffers["level_up"] = generateLevelUpPcm()
        }
    }

    fun playLaser(heavy: Boolean = false) {
        if (!soundEnabled) return
        val key = if (heavy) "laser_heavy" else "laser"
        playBuffer(key)
    }

    fun playExplosion(large: Boolean = false) {
        if (!soundEnabled) return
        val key = if (large) "explosion_large" else "explosion_small"
        playBuffer(key)
        vibrate(if (large) 70 else 30)
    }

    fun playHit() {
        if (!soundEnabled) return
        playBuffer("hit")
        vibrate(45)
    }

    fun playPowerup() {
        if (!soundEnabled) return
        playBuffer("powerup")
        vibrate(25)
    }

    fun playBomb() {
        if (!soundEnabled) return
        playBuffer("bomb")
        vibrate(120)
    }

    fun playGameOver() {
        if (!soundEnabled) return
        playBuffer("game_over")
        vibrate(150)
    }

    fun playLevelUp() {
        if (!soundEnabled) return
        playBuffer("level_up")
        vibrate(60)
    }

    private fun playBuffer(key: String) {
        val buffer = soundBuffers[key] ?: return
        audioScope.launch {
            try {
                val minSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(buffer.size * 2, minSize)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                // Sleep duration of playback then release
                val sleepMs = ((buffer.size.toFloat() / sampleRate) * 1000).toLong() + 30
                kotlinx.coroutines.delay(sleepMs)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Ignore audio hardware transient exceptions
            }
        }
    }

    private fun vibrate(durationMs: Long) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore vibration permissions or hardware issues
        }
    }

    // --- Sound Synthesis Algorithms ---

    private fun generateLaserPcm(): ShortArray {
        val durationMs = 120
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        val startFreq = 950.0
        val endFreq = 250.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * t
            val envelope = (1.0 - t)
            phase += 2.0 * PI * currentFreq / sampleRate
            val sample = sin(phase) * envelope * 0.7
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateHeavyLaserPcm(): ShortArray {
        val durationMs = 170
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        val startFreq = 500.0
        val endFreq = 120.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * t
            val envelope = (1.0 - t)
            phase1 += 2.0 * PI * currentFreq / sampleRate
            phase2 += 2.0 * PI * (currentFreq * 1.5) / sampleRate
            val sample = (sin(phase1) * 0.5 + sin(phase2) * 0.3) * envelope * 0.8
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateExplosionPcm(durationMs: Int, decayRate: Float): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var lastNoise = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val whiteNoise = (Random.nextDouble() * 2.0 - 1.0)
            // Low-pass filter for deeper explosion rumble
            val filtered = 0.75 * lastNoise + 0.25 * whiteNoise
            lastNoise = filtered

            val envelope = exp(-t * decayRate)
            val sample = filtered * envelope * 0.85
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateHitPcm(): ShortArray {
        val durationMs = 80
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var phase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            phase += 2.0 * PI * 130.0 / sampleRate
            val envelope = (1.0 - t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.3
            val sample = (sin(phase) * 0.7 + noise) * envelope * 0.6
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generatePowerupPcm(): ShortArray {
        val durationMs = 300
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val frequencies = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
        var currentPhase = 0.0

        for (i in 0 until numSamples) {
            val noteIndex = ((i.toDouble() / numSamples) * frequencies.size).toInt().coerceIn(0, frequencies.size - 1)
            val freq = frequencies[noteIndex]
            val noteProgress = (i.toDouble() % (numSamples / frequencies.size)) / (numSamples / frequencies.size)
            val envelope = (1.0 - noteProgress * 0.4)
            currentPhase += 2.0 * PI * freq / sampleRate
            val sample = sin(currentPhase) * envelope * 0.6
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateBombPcm(): ShortArray {
        val durationMs = 500
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        var rumblePhase = 0.0
        var ringPhase = 0.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            rumblePhase += 2.0 * PI * (65.0 * (1.0 - t * 0.5)) / sampleRate
            ringPhase += 2.0 * PI * (1200.0 * (1.0 - t * 0.8)) / sampleRate
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.4
            val envelope = exp(-t * 2.5)
            val sample = (sin(rumblePhase) * 0.5 + sin(ringPhase) * 0.2 + noise) * envelope * 0.9
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateGameOverPcm(): ShortArray {
        val durationMs = 600
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val frequencies = doubleArrayOf(392.0, 369.99, 329.63, 261.63) // G4, F#4, E4, C4
        var phase = 0.0

        for (i in 0 until numSamples) {
            val noteIndex = ((i.toDouble() / numSamples) * frequencies.size).toInt().coerceIn(0, frequencies.size - 1)
            val freq = frequencies[noteIndex]
            phase += 2.0 * PI * freq / sampleRate
            val t = i.toDouble() / numSamples
            val sample = sin(phase) * (1.0 - t * 0.8) * 0.65
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateLevelUpPcm(): ShortArray {
        val durationMs = 350
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val frequencies = doubleArrayOf(440.0, 554.37, 659.25, 880.0) // A4, C#5, E5, A5
        var phase = 0.0

        for (i in 0 until numSamples) {
            val noteIndex = ((i.toDouble() / numSamples) * frequencies.size).toInt().coerceIn(0, frequencies.size - 1)
            val freq = frequencies[noteIndex]
            phase += 2.0 * PI * freq / sampleRate
            val sample = sin(phase) * 0.7
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
