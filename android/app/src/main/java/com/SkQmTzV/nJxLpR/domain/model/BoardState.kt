package com.SkQmTzV.nJxLpR.domain.model

data class BoardState(
    val size: Int,
    val grid: List<JellyColor>,
    val fixed: Set<Cell>,
    val linked: Set<JellyColor>
) {
    fun colorAt(row: Int, col: Int): JellyColor = grid[row * size + col]

    fun colorAt(cell: Cell): JellyColor = colorAt(cell.row, cell.col)

    fun isFixed(cell: Cell): Boolean = fixed.contains(cell)

    fun contains(cell: Cell): Boolean =
        cell.row in 0 until size && cell.col in 0 until size

    fun withGrid(next: List<JellyColor>): BoardState = copy(grid = next)

    fun withLinked(next: Set<JellyColor>): BoardState = copy(linked = next)

    companion object {
        val SOURCES: Map<JellyColor, Cell> = mapOf(
            JellyColor.PINK to Cell(0, 0),
            JellyColor.LEMON to Cell(0, 5),
            JellyColor.MINT to Cell(5, 0),
            JellyColor.BERRY to Cell(5, 5)
        )

        val PETALS: Map<JellyColor, Cell> = mapOf(
            JellyColor.PINK to Cell(2, 2),
            JellyColor.LEMON to Cell(2, 3),
            JellyColor.MINT to Cell(3, 2),
            JellyColor.BERRY to Cell(3, 3)
        )

        val FIXED_CELLS: Set<Cell> = SOURCES.values.toSet() + PETALS.values.toSet()
    }
}
