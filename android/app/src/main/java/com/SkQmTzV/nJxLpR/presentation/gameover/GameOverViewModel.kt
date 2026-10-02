package com.SkQmTzV.nJxLpR.presentation.gameover

import androidx.lifecycle.ViewModel
import com.SkQmTzV.nJxLpR.domain.model.BoardState
import com.SkQmTzV.nJxLpR.domain.model.RoundResult
import com.SkQmTzV.nJxLpR.domain.repository.LevelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameOverViewModel(
    private val outcome: RoundResult,
    private val levelRepository: LevelRepository
) : ViewModel() {

    private val mutableState = MutableStateFlow(outcome)
    val state: StateFlow<RoundResult> = mutableState.asStateFlow()

    fun previewBoard(): BoardState = BoardState(
        size = BOARD_SIDE,
        grid = outcome.finalGrid,
        fixed = BoardState.FIXED_CELLS,
        linked = emptySet()
    )

    fun nextLevelId(): Int {
        val candidate = outcome.levelId + 1
        return if (candidate <= levelRepository.count()) candidate else outcome.levelId
    }

    fun canAdvance(): Boolean = outcome.win && outcome.levelId < levelRepository.count()

    companion object {
        private const val BOARD_SIDE = 6
    }
}
