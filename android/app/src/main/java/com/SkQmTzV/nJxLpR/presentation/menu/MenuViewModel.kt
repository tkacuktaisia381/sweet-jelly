package com.SkQmTzV.nJxLpR.presentation.menu

import androidx.lifecycle.ViewModel
import com.SkQmTzV.nJxLpR.core.config.GameConfig
import com.SkQmTzV.nJxLpR.domain.repository.LevelRepository
import com.SkQmTzV.nJxLpR.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel(
    private val levelRepository: LevelRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(load())
    val state: StateFlow<MenuUiState> = mutableState.asStateFlow()

    fun refresh() {
        mutableState.value = load()
    }

    fun tutorialSeen(): Boolean = progressRepository.tutorialSeen()

    fun markTutorialSeen() {
        progressRepository.markTutorialSeen()
    }

    private fun load(): MenuUiState {
        val stars = progressRepository.totalStars()
        val solved = progressRepository.solvedCount()
        val next = progressRepository.nextLevelId()
        val best = progressRepository.all().lastOrNull { it.bestStars > 0 }?.levelId ?: 1
        return MenuUiState(
            totalStars = stars,
            maxStars = levelRepository.count() * GameConfig.MAX_STARS_PER_LEVEL,
            solvedCount = solved,
            bestLevelId = best,
            nextLevelId = next,
            nextLevelName = levelRepository.level(next).name,
            fresh = stars == 0 && solved == 0
        )
    }
}
