package com.SkQmTzV.nJxLpR.domain.repository

import com.SkQmTzV.nJxLpR.domain.model.LevelDefinition

interface LevelRepository {
    fun levels(): List<LevelDefinition>

    fun level(id: Int): LevelDefinition

    fun count(): Int
}
