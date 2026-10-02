package com.SkQmTzV.nJxLpR.data.repository

import com.SkQmTzV.nJxLpR.data.local.JellyPreferences
import com.SkQmTzV.nJxLpR.data.sample.SampleData
import com.SkQmTzV.nJxLpR.domain.model.LevelProgress
import com.SkQmTzV.nJxLpR.domain.repository.ProgressRepository

class ProgressRepositoryImpl(private val preferences: JellyPreferences) : ProgressRepository {

    override fun progress(levelId: Int): LevelProgress = LevelProgress(
        levelId = levelId,
        bestStars = preferences.stars(levelId),
        bestScore = preferences.score(levelId),
        unlocked = preferences.unlocked(levelId)
    )

    override fun all(): List<LevelProgress> =
        (1..SampleData.LEVEL_COUNT).map { progress(it) }

    override fun store(levelId: Int, stars: Int, score: Int) {
        if (stars > preferences.stars(levelId)) preferences.setStars(levelId, stars)
        if (score > preferences.score(levelId)) preferences.setScore(levelId, score)
    }

    override fun unlock(levelId: Int) {
        preferences.setUnlocked(levelId)
    }

    override fun totalStars(): Int = all().sumOf { it.bestStars }

    override fun solvedCount(): Int = all().count { it.bestStars > 0 }

    override fun nextLevelId(): Int {
        val pending = all().firstOrNull { it.unlocked && it.bestStars == 0 }
        if (pending != null) return pending.levelId
        val lastUnlocked = all().lastOrNull { it.unlocked }
        return lastUnlocked?.levelId ?: 1
    }

    override fun tutorialSeen(): Boolean = preferences.tutorialSeen()

    override fun markTutorialSeen() {
        preferences.setTutorialSeen()
    }

    override fun soundEnabled(): Boolean = preferences.sound()

    override fun setSoundEnabled(enabled: Boolean) {
        preferences.setSound(enabled)
    }

    override fun vibrationEnabled(): Boolean = preferences.vibration()

    override fun setVibrationEnabled(enabled: Boolean) {
        preferences.setVibration(enabled)
    }

    override fun resetProgress() {
        preferences.clearProgress()
    }
}
