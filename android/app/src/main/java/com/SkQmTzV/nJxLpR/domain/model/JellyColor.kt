package com.SkQmTzV.nJxLpR.domain.model

enum class JellyColor(val code: Char, val argb: Int, val goal: Boolean) {
    PINK('P', 0xFFF7A9C4.toInt(), true),
    LEMON('Y', 0xFFFFCF56.toInt(), true),
    MINT('M', 0xFF85D6B1.toInt(), true),
    BERRY('B', 0xFF89B6F2.toInt(), true),
    GRAPE('G', 0xFF7653A6.toInt(), false);

    companion object {
        val GOALS: List<JellyColor> = listOf(PINK, LEMON, MINT, BERRY)

        fun fromCode(code: Char): JellyColor = values().firstOrNull { it.code == code } ?: GRAPE
    }
}
