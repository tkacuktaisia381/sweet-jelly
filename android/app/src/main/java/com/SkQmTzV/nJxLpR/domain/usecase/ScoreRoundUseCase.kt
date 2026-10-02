package com.SkQmTzV.nJxLpR.domain.usecase

class ScoreRoundUseCase {

    fun score(linkedCount: Int, movesLeft: Int, win: Boolean): Int {
        val bonus = if (win) WIN_BONUS else 0
        return linkedCount * PER_LINK + movesLeft * PER_MOVE_LEFT + bonus
    }

    fun stars(win: Boolean, movesLeft: Int, moveLimit: Int, hintsUsed: Int): Int {
        if (!win) return 0
        val limit = if (moveLimit <= 0) 1 else moveLimit
        val ratio = movesLeft.toFloat() / limit.toFloat()
        val base = when {
            ratio >= 0.40f -> 3
            ratio >= 0.15f -> 2
            else -> 1
        }
        val reduced = base - hintsUsed
        return if (reduced < 1) 1 else reduced
    }

    companion object {
        const val PER_LINK = 150
        const val PER_MOVE_LEFT = 40
        const val WIN_BONUS = 200
    }
}
