package com.SkQmTzV.nJxLpR.data.repository

import android.content.Context
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.data.sample.SampleData
import com.SkQmTzV.nJxLpR.domain.model.LevelDefinition
import com.SkQmTzV.nJxLpR.domain.repository.LevelRepository

class LevelRepositoryImpl(context: Context) : LevelRepository {

    private val cache: List<LevelDefinition> = build(context)

    override fun levels(): List<LevelDefinition> = cache

    override fun level(id: Int): LevelDefinition {
        val found = cache.firstOrNull { it.id == id }
        return found ?: cache.first()
    }

    override fun count(): Int = cache.size

    private fun build(context: Context): List<LevelDefinition> {
        val names = context.resources.getStringArray(R.array.level_names)
        val levels = ArrayList<LevelDefinition>(SampleData.LEVEL_COUNT)
        for (id in 1..SampleData.LEVEL_COUNT) {
            val label = if (id - 1 < names.size) {
                names[id - 1]
            } else {
                SampleData.FALLBACK_NAMES[(id - 1) % SampleData.FALLBACK_NAMES.size]
            }
            levels.add(
                LevelDefinition(
                    id = id,
                    name = label,
                    patternRows = SampleData.patternFor(id),
                    scrambleDepth = SampleData.depthFor(id),
                    moveLimit = SampleData.moveLimitFor(id)
                )
            )
        }
        return levels
    }
}
