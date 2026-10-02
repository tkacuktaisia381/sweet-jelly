package com.SkQmTzV.nJxLpR.domain.usecase

import com.SkQmTzV.nJxLpR.domain.model.BoardState
import com.SkQmTzV.nJxLpR.domain.model.BuiltBoard
import com.SkQmTzV.nJxLpR.domain.model.Cell
import com.SkQmTzV.nJxLpR.domain.model.JellyColor
import com.SkQmTzV.nJxLpR.domain.model.LevelDefinition
import com.SkQmTzV.nJxLpR.domain.model.Swap
import kotlin.random.Random

class BuildBoardUseCase(
    private val applySwap: ApplySwapUseCase,
    private val evaluate: EvaluateConnectionsUseCase
) {

    operator fun invoke(level: LevelDefinition): BuiltBoard {
        val solved = BoardState(
            size = SIZE,
            grid = parse(level.patternRows),
            fixed = BoardState.FIXED_CELLS,
            linked = JellyColor.GOALS.toSet()
        )

        var depth = level.scrambleDepth
        var attempt = 0
        var fallback: Pair<BoardState, List<Swap>>? = null

        while (attempt < MAX_ATTEMPTS) {
            val seed = SEED_FACTOR * level.id + SEED_OFFSET + attempt
            val scrambled = scramble(solved, depth, Random(seed))
            if (fallback == null) fallback = scrambled
            val linked = evaluate(scrambled.first)
            if (linked.size < JellyColor.GOALS.size && scrambled.second.isNotEmpty()) {
                return BuiltBoard(
                    start = scrambled.first.withLinked(linked),
                    solved = solved,
                    scrambleSwaps = scrambled.second
                )
            }
            attempt++
            if (attempt % RETRIES_PER_DEPTH == 0) depth += 2
        }

        val safe = fallback ?: (solved to emptyList())
        return BuiltBoard(
            start = safe.first.withLinked(evaluate(safe.first)),
            solved = solved,
            scrambleSwaps = safe.second
        )
    }

    private fun parse(rows: List<String>): List<JellyColor> {
        val grid = ArrayList<JellyColor>(SIZE * SIZE)
        for (row in rows) {
            for (code in row) {
                grid.add(JellyColor.fromCode(code))
            }
        }
        return grid
    }

    private fun scramble(
        solved: BoardState,
        depth: Int,
        random: Random
    ): Pair<BoardState, List<Swap>> {
        var board = solved
        val swaps = ArrayList<Swap>(depth)
        var guard = 0
        while (swaps.size < depth && guard < GUARD_LIMIT) {
            guard++
            val origin = Cell(random.nextInt(SIZE), random.nextInt(SIZE))
            val target = when (random.nextInt(4)) {
                0 -> Cell(origin.row - 1, origin.col)
                1 -> Cell(origin.row + 1, origin.col)
                2 -> Cell(origin.row, origin.col - 1)
                else -> Cell(origin.row, origin.col + 1)
            }
            val swap = Swap(origin, target)
            if (!applySwap.isLegal(board, swap)) continue
            if (board.colorAt(origin) == board.colorAt(target)) continue
            board = applySwap(board, swap)
            swaps.add(swap)
        }
        return board to swaps
    }

    companion object {
        const val SIZE = 6
        private const val SEED_FACTOR = 7919L
        private const val SEED_OFFSET = 13L
        private const val MAX_ATTEMPTS = 60
        private const val RETRIES_PER_DEPTH = 20
        private const val GUARD_LIMIT = 600
    }
}
