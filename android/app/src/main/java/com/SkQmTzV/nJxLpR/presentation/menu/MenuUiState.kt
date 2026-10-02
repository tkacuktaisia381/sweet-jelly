package com.SkQmTzV.nJxLpR.presentation.menu

data class MenuUiState(
    val totalStars: Int,
    val maxStars: Int,
    val solvedCount: Int,
    val bestLevelId: Int,
    val nextLevelId: Int,
    val nextLevelName: String,
    val fresh: Boolean
)
