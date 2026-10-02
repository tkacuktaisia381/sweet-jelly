package com.SkQmTzV.nJxLpR.data.local

import android.content.Context
import android.content.SharedPreferences

class JellyPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)

    fun stars(levelId: Int): Int = prefs.getInt(KEY_STARS + levelId, 0)

    fun setStars(levelId: Int, value: Int) {
        prefs.edit().putInt(KEY_STARS + levelId, value).apply()
    }

    fun score(levelId: Int): Int = prefs.getInt(KEY_SCORE + levelId, 0)

    fun setScore(levelId: Int, value: Int) {
        prefs.edit().putInt(KEY_SCORE + levelId, value).apply()
    }

    fun unlocked(levelId: Int): Boolean =
        levelId <= 1 || prefs.getBoolean(KEY_UNLOCKED + levelId, false)

    fun setUnlocked(levelId: Int) {
        prefs.edit().putBoolean(KEY_UNLOCKED + levelId, true).apply()
    }

    fun tutorialSeen(): Boolean = prefs.getBoolean(KEY_TUTORIAL, false)

    fun setTutorialSeen() {
        prefs.edit().putBoolean(KEY_TUTORIAL, true).apply()
    }

    fun sound(): Boolean = prefs.getBoolean(KEY_SOUND, true)

    fun setSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
    }

    fun vibration(): Boolean = prefs.getBoolean(KEY_VIBRATION, true)

    fun setVibration(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    fun clearProgress() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val STORE_NAME = "sweet_jelly_store"
        private const val KEY_STARS = "stars_"
        private const val KEY_SCORE = "score_"
        private const val KEY_UNLOCKED = "unlocked_"
        private const val KEY_TUTORIAL = "tutorial_seen"
        private const val KEY_SOUND = "sound_on"
        private const val KEY_VIBRATION = "vibration_on"
    }
}
