package com.SkQmTzV.nJxLpR.core.di

import android.content.Context
import com.SkQmTzV.nJxLpR.data.local.JellyPreferences
import com.SkQmTzV.nJxLpR.data.repository.LevelRepositoryImpl
import com.SkQmTzV.nJxLpR.data.repository.ProgressRepositoryImpl
import com.SkQmTzV.nJxLpR.domain.repository.LevelRepository
import com.SkQmTzV.nJxLpR.domain.repository.ProgressRepository
import com.SkQmTzV.nJxLpR.domain.usecase.ApplySwapUseCase
import com.SkQmTzV.nJxLpR.domain.usecase.BuildBoardUseCase
import com.SkQmTzV.nJxLpR.domain.usecase.EvaluateConnectionsUseCase
import com.SkQmTzV.nJxLpR.domain.usecase.SaveProgressUseCase
import com.SkQmTzV.nJxLpR.domain.usecase.ScoreRoundUseCase

object ServiceLocator {

    private var appContext: Context? = null
    private var preferences: JellyPreferences? = null
    private var levels: LevelRepository? = null
    private var progress: ProgressRepository? = null

    private val applySwapUseCase = ApplySwapUseCase()
    private val evaluateUseCase = EvaluateConnectionsUseCase()
    private val scoreUseCase = ScoreRoundUseCase()

    fun init(context: Context) {
        if (appContext == null) appContext = context.applicationContext
    }

    fun preferences(context: Context): JellyPreferences {
        init(context)
        val existing = preferences
        if (existing != null) return existing
        val created = JellyPreferences(resolve(context))
        preferences = created
        return created
    }

    fun levelRepository(context: Context): LevelRepository {
        init(context)
        val existing = levels
        if (existing != null) return existing
        val created = LevelRepositoryImpl(resolve(context))
        levels = created
        return created
    }

    fun progressRepository(context: Context): ProgressRepository {
        init(context)
        val existing = progress
        if (existing != null) return existing
        val created = ProgressRepositoryImpl(preferences(context))
        progress = created
        return created
    }

    fun applySwap(): ApplySwapUseCase = applySwapUseCase

    fun evaluateConnections(): EvaluateConnectionsUseCase = evaluateUseCase

    fun scoreRound(): ScoreRoundUseCase = scoreUseCase

    fun buildBoard(): BuildBoardUseCase = BuildBoardUseCase(applySwapUseCase, evaluateUseCase)

    fun saveProgress(context: Context): SaveProgressUseCase =
        SaveProgressUseCase(progressRepository(context))

    private fun resolve(context: Context): Context = appContext ?: context.applicationContext
}
