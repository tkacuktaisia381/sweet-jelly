package com.SkQmTzV.nJxLpR.presentation.game

import com.SkQmTzV.nJxLpR.domain.model.BoardState
import com.SkQmTzV.nJxLpR.domain.model.Cell
import com.SkQmTzV.nJxLpR.domain.model.JellyColor

data class GameUiState(
    val status: GameStatus,
    val board: BoardState,
    val levelId: Int,
    val levelName: String,
    val movesUsed: Int,
    val moveLimit: Int,
    val hintsLeft: Int,
    val selected: Cell?,
    val highlight: Set<Cell>,
    val linked: Set<JellyColor>
) {
    val movesLeft: Int
        get() = if (moveLimit - movesUsed > 0) moveLimit - movesUsed else 0
}
