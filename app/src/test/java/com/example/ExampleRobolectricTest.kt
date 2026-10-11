package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundManager
import com.example.data.HighScoreRepository
import com.example.game.GameEngine
import com.example.model.ScreenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AstroStrike", appName)
    }

    @Test
    fun `verify game engine initialization and new game launch`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = HighScoreRepository(context)
        val soundManager = SoundManager(context)
        val engine = GameEngine(soundManager, repository)

        engine.setDimensions(1080f, 1920f)
        assertEquals(ScreenState.TITLE, engine.screenState)

        engine.startNewGame()
        assertEquals(ScreenState.PLAYING, engine.screenState)
        assertEquals(3, engine.player.lives)
        assertEquals(100, engine.player.health)
        assertEquals(1, engine.stats.level)
        assertEquals(0, engine.stats.score)
    }

    @Test
    fun `verify high score repository persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = HighScoreRepository(context)

        val isHigh = repository.saveScore(score = 1500, level = 2, asteroids = 15)
        assertTrue(isHigh)
        assertEquals(1500, repository.getHighScore())
    }
}
