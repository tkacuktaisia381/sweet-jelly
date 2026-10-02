package com.SkQmTzV.nJxLpR.domain.model

data class LevelDefinition(
    val id: Int,
    val name: String,
    val patternRows: List<String>,
    val scrambleDepth: Int,
    val moveLimit: Int
)
