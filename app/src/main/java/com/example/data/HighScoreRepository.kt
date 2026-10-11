package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class HighScoreEntry(
    val score: Int,
    val level: Int,
    val asteroidsDestroyed: Int,
    val timestamp: Long
)

class HighScoreRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("astro_strike_prefs", Context.MODE_PRIVATE)

    fun getHighScore(): Int {
        return prefs.getInt(KEY_HIGH_SCORE, 0)
    }

    fun saveScore(score: Int, level: Int, asteroids: Int): Boolean {
        val currentHigh = getHighScore()
        val isNewHigh = score > currentHigh
        if (isNewHigh) {
            prefs.edit().putInt(KEY_HIGH_SCORE, score).apply()
        }

        // Save to leaderboard history (top 10)
        val history = getLeaderboard().toMutableList()
        history.add(HighScoreEntry(score, level, asteroids, System.currentTimeMillis()))
        history.sortByDescending { it.score }
        val trimmed = history.take(10)
        saveLeaderboard(trimmed)

        // Increment lifetime stats
        val totalKills = prefs.getInt(KEY_TOTAL_KILLS, 0) + asteroids
        val totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1
        prefs.edit()
            .putInt(KEY_TOTAL_KILLS, totalKills)
            .putInt(KEY_TOTAL_GAMES, totalGames)
            .apply()

        return isNewHigh
    }

    fun getLeaderboard(): List<HighScoreEntry> {
        val jsonStr = prefs.getString(KEY_LEADERBOARD, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<HighScoreEntry>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HighScoreEntry(
                        score = obj.optInt("score", 0),
                        level = obj.optInt("level", 1),
                        asteroidsDestroyed = obj.optInt("asteroids", 0),
                        timestamp = obj.optLong("time", 0L)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveLeaderboard(entries: List<HighScoreEntry>) {
        val array = JSONArray()
        for (entry in entries) {
            val obj = JSONObject().apply {
                put("score", entry.score)
                put("level", entry.level)
                put("asteroids", entry.asteroidsDestroyed)
                put("time", entry.timestamp)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_LEADERBOARD, array.toString()).apply()
    }

    fun getTotalKills(): Int = prefs.getInt(KEY_TOTAL_KILLS, 0)
    fun getTotalGames(): Int = prefs.getInt(KEY_TOTAL_GAMES, 0)

    // Settings
    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND, true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND, enabled).apply()

    fun isHapticsEnabled(): Boolean = prefs.getBoolean(KEY_HAPTICS, true)
    fun setHapticsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()

    fun isAutoFireEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_FIRE, true)
    fun setAutoFireEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_FIRE, enabled).apply()

    fun getControlMode(): ControlMode {
        val name = prefs.getString(KEY_CONTROL_MODE, ControlMode.DIRECT_DRAG.name)
        return try {
            ControlMode.valueOf(name ?: ControlMode.DIRECT_DRAG.name)
        } catch (_: Exception) {
            ControlMode.DIRECT_DRAG
        }
    }

    fun setControlMode(mode: ControlMode) {
        prefs.edit().putString(KEY_CONTROL_MODE, mode.name).apply()
    }

    companion object {
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_LEADERBOARD = "leaderboard_json"
        private const val KEY_TOTAL_KILLS = "total_kills"
        private const val KEY_TOTAL_GAMES = "total_games"
        private const val KEY_SOUND = "pref_sound"
        private const val KEY_HAPTICS = "pref_haptics"
        private const val KEY_AUTO_FIRE = "pref_auto_fire"
        private const val KEY_CONTROL_MODE = "pref_control_mode"
    }
}

enum class ControlMode(val title: String) {
    DIRECT_DRAG("Direct Drag"),
    VIRTUAL_BUTTONS("Arcade Arrows")
}
