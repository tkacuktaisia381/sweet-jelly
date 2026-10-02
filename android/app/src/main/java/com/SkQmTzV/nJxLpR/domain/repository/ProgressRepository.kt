package com.SkQmTzV.nJxLpR.domain.repository

import com.SkQmTzV.nJxLpR.domain.model.LevelProgress

interface ProgressRepository {
    fun progress(levelId: Int): LevelProgress

    fun all(): List<LevelProgress>

    fun store(levelId: Int, stars: Int, score: Int)

    fun unlock(levelId: Int)

    fun totalStars(): Int

    fun solvedCount(): Int

    fun nextLevelId(): Int

    fun tutorialSeen(): Boolean

    fun markTutorialSeen()

    fun soundEnabled(): Boolean

    fun setSoundEnabled(enabled: Boolean)

    fun vibrationEnabled(): Boolean

    fun setVibrationEnabled(enabled: Boolean)

    fun resetProgress()
}
