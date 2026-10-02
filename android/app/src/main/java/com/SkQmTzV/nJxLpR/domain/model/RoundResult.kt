package com.SkQmTzV.nJxLpR.domain.model

data class RoundResult(
    val levelId: Int,
    val levelName: String,
    val win: Boolean,
    val score: Int,
    val stars: Int,
    val movesLeft: Int,
    val linkedCount: Int,
    val hasNext: Boolean,
    val finalGrid: List<JellyColor>
)
