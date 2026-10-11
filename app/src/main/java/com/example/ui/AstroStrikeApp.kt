package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.audio.SoundManager
import com.example.data.ControlMode
import com.example.data.HighScoreRepository
import com.example.game.GameEngine
import com.example.model.ScreenState
import com.example.ui.components.ControlsOverlay
import com.example.ui.components.GameCanvas
import com.example.ui.components.GameHud
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.LeaderboardDialog
import com.example.ui.screens.PauseOverlay
import com.example.ui.screens.TitleScreen
import kotlinx.coroutines.isActive

@Composable
fun AstroStrikeApp() {
    val context = LocalContext.current
    val repository = remember { HighScoreRepository(context) }
    val soundManager = remember {
        SoundManager(context).apply {
            soundEnabled = repository.isSoundEnabled()
            hapticsEnabled = repository.isHapticsEnabled()
        }
    }
    val engine = remember { GameEngine(soundManager, repository) }

    var screenState by remember { mutableStateOf(engine.screenState) }
    var showLeaderboard by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(repository.isSoundEnabled()) }
    var controlMode by remember { mutableStateOf(repository.getControlMode()) }
    var autoFire by remember { mutableStateOf(repository.isAutoFireEnabled()) }

    // Synchronize engine state changes
    LaunchedEffect(engine.screenState) {
        screenState = engine.screenState
    }

    // High performance game frame loop
    LaunchedEffect(screenState) {
        var lastNanos = System.nanoTime()
        while (isActive) {
            withFrameNanos { nowNanos ->
                val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastNanos = nowNanos

                if (screenState == ScreenState.PLAYING) {
                    engine.update(dt)
                    if (repository.isAutoFireEnabled()) {
                        engine.triggerPlayerFire(force = false)
                    }
                    if (engine.screenState != screenState) {
                        screenState = engine.screenState
                    }
                } else {
                    // Update starfield & particles for title / pause background
                    engine.update(dt * 0.5f)
                }
            }
        }
    }

    // Android System Back Button handling
    BackHandler(enabled = screenState != ScreenState.TITLE) {
        when (screenState) {
            ScreenState.PLAYING -> {
                engine.screenState = ScreenState.PAUSED
                screenState = ScreenState.PAUSED
            }
            ScreenState.PAUSED -> {
                engine.screenState = ScreenState.PLAYING
                screenState = ScreenState.PLAYING
            }
            ScreenState.GAME_OVER -> {
                engine.screenState = ScreenState.TITLE
                screenState = ScreenState.TITLE
            }
            ScreenState.TITLE -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        // ALWAYS RENDER BACKGROUND CANVAS
        GameCanvas(
            engine = engine,
            controlMode = repository.getControlMode(),
            autoFire = repository.isAutoFireEnabled()
        )

        // SCREEN ROUTING
        when (screenState) {
            ScreenState.TITLE -> {
                TitleScreen(
                    highScore = repository.getHighScore(),
                    repository = repository,
                    onStartGame = {
                        controlMode = repository.getControlMode()
                        autoFire = repository.isAutoFireEnabled()
                        soundEnabled = repository.isSoundEnabled()
                        soundManager.soundEnabled = soundEnabled
                        soundManager.hapticsEnabled = repository.isHapticsEnabled()
                        engine.startNewGame()
                        screenState = ScreenState.PLAYING
                    },
                    onOpenLeaderboard = { showLeaderboard = true }
                )
            }

            ScreenState.PLAYING -> {
                GameHud(
                    engine = engine,
                    onPauseClick = {
                        engine.screenState = ScreenState.PAUSED
                        screenState = ScreenState.PAUSED
                    },
                    onToggleSound = {
                        val newSound = !soundEnabled
                        soundEnabled = newSound
                        repository.setSoundEnabled(newSound)
                        soundManager.soundEnabled = newSound
                    },
                    soundEnabled = soundEnabled
                )

                ControlsOverlay(
                    engine = engine,
                    controlMode = repository.getControlMode(),
                    autoFire = repository.isAutoFireEnabled()
                )
            }

            ScreenState.PAUSED -> {
                PauseOverlay(
                    repository = repository,
                    onResume = {
                        soundEnabled = repository.isSoundEnabled()
                        soundManager.soundEnabled = soundEnabled
                        soundManager.hapticsEnabled = repository.isHapticsEnabled()
                        engine.screenState = ScreenState.PLAYING
                        screenState = ScreenState.PLAYING
                    },
                    onRestart = {
                        soundEnabled = repository.isSoundEnabled()
                        soundManager.soundEnabled = soundEnabled
                        soundManager.hapticsEnabled = repository.isHapticsEnabled()
                        engine.startNewGame()
                        screenState = ScreenState.PLAYING
                    },
                    onMainMenu = {
                        engine.screenState = ScreenState.TITLE
                        screenState = ScreenState.TITLE
                    }
                )
            }

            ScreenState.GAME_OVER -> {
                GameOverScreen(
                    stats = engine.stats,
                    isNewHighScore = engine.isNewHighScore,
                    onRestart = {
                        soundEnabled = repository.isSoundEnabled()
                        soundManager.soundEnabled = soundEnabled
                        soundManager.hapticsEnabled = repository.isHapticsEnabled()
                        engine.startNewGame()
                        screenState = ScreenState.PLAYING
                    },
                    onMainMenu = {
                        engine.screenState = ScreenState.TITLE
                        screenState = ScreenState.TITLE
                    }
                )
            }
        }

        // LEADERBOARD DIALOG
        if (showLeaderboard) {
            LeaderboardDialog(
                repository = repository,
                onDismiss = { showLeaderboard = false }
            )
        }
    }
}
