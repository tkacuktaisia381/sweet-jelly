package com.SkQmTzV.nJxLpR.domain.model

data class Cell(val row: Int, val col: Int) {
    fun isNeighbourOf(other: Cell): Boolean {
        val dr = if (row > other.row) row - other.row else other.row - row
        val dc = if (col > other.col) col - other.col else other.col - col
        return dr + dc == 1
    }
}
