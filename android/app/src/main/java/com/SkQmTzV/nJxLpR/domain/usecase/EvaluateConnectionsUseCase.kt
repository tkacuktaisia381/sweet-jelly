package com.SkQmTzV.nJxLpR.domain.usecase

import com.SkQmTzV.nJxLpR.domain.model.BoardState
import com.SkQmTzV.nJxLpR.domain.model.Cell
import com.SkQmTzV.nJxLpR.domain.model.JellyColor

class EvaluateConnectionsUseCase {

    operator fun invoke(board: BoardState): Set<JellyColor> {
        val linked = mutableSetOf<JellyColor>()
        for (colour in JellyColor.GOALS) {
            if (pathOf(board, colour).isNotEmpty()) linked.add(colour)
        }
        return linked
    }

    fun pathOf(board: BoardState, colour: JellyColor): List<Cell> {
        val source = BoardState.SOURCES[colour] ?: return emptyList()
        val petal = BoardState.PETALS[colour] ?: return emptyList()
        if (board.colorAt(source) != colour || board.colorAt(petal) != colour) return emptyList()

        val parents = HashMap<Cell, Cell>()
        val seen = mutableSetOf(source)
        val queue = ArrayDeque<Cell>()
        queue.addLast(source)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == petal) return trace(parents, source, petal)
            for (next in neighbours(board, current)) {
                if (next in seen) continue
                if (board.colorAt(next) != colour) continue
                seen.add(next)
                parents[next] = current
                queue.addLast(next)
            }
        }
        return emptyList()
    }

    private fun neighbours(board: BoardState, cell: Cell): List<Cell> {
        val candidates = listOf(
            Cell(cell.row - 1, cell.col),
            Cell(cell.row + 1, cell.col),
            Cell(cell.row, cell.col - 1),
            Cell(cell.row, cell.col + 1)
        )
        return candidates.filter { board.contains(it) }
    }

    private fun trace(parents: Map<Cell, Cell>, source: Cell, petal: Cell): List<Cell> {
        val chain = mutableListOf(petal)
        var cursor = petal
        while (cursor != source) {
            val parent = parents[cursor] ?: return chain.reversed()
            chain.add(parent)
            cursor = parent
        }
        return chain.reversed()
    }
}
