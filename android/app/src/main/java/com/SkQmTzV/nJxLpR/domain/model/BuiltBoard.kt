package com.SkQmTzV.nJxLpR.domain.model

data class BuiltBoard(
    val start: BoardState,
    val solved: BoardState,
    val scrambleSwaps: List<Swap>
)
