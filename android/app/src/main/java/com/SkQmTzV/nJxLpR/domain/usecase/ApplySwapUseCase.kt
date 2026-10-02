package com.SkQmTzV.nJxLpR.domain.usecase

import com.SkQmTzV.nJxLpR.domain.model.BoardState
import com.SkQmTzV.nJxLpR.domain.model.Cell
import com.SkQmTzV.nJxLpR.domain.model.Swap

class ApplySwapUseCase {

    fun isLegal(board: BoardState, swap: Swap): Boolean {
        if (!board.contains(swap.a) || !board.contains(swap.b)) return false
        if (board.isFixed(swap.a) || board.isFixed(swap.b)) return false
        if (!swap.a.isNeighbourOf(swap.b)) return false
        return true
    }

    fun isSwappable(board: BoardState, cell: Cell): Boolean =
        board.contains(cell) && !board.isFixed(cell)

    operator fun invoke(board: BoardState, swap: Swap): BoardState {
        val next = board.grid.toMutableList()
        val indexA = index(board, swap.a)
        val indexB = index(board, swap.b)
        val held = next[indexA]
        next[indexA] = next[indexB]
        next[indexB] = held
        return board.withGrid(next)
    }

    private fun index(board: BoardState, cell: Cell): Int = cell.row * board.size + cell.col
}
