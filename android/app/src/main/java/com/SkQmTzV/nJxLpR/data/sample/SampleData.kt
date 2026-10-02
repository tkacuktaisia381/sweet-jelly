package com.SkQmTzV.nJxLpR.data.sample

object SampleData {

    val CROSS_GLASS: List<String> = listOf(
        "PPPYYY",
        "GGPYGG",
        "GGPYGG",
        "GGMBGG",
        "GGMBGG",
        "MMMBBB"
    )

    val PETAL_SPIRAL: List<String> = listOf(
        "PPGGYY",
        "GPGGYG",
        "GPPYYG",
        "GMMBBG",
        "GMGGBG",
        "MMGGBB"
    )

    val MEADOW_GLASS: List<String> = listOf(
        "PGGGGY",
        "PGGGGY",
        "PPPYYY",
        "MMMBBB",
        "MGGGGB",
        "MGGGGB"
    )

    val BERRY_ZIGZAG: List<String> = listOf(
        "PPGGYY",
        "GPPGGY",
        "GGPYYY",
        "GMMBBG",
        "MMGGBB",
        "MGGGGB"
    )

    val PATTERNS: List<List<String>> = listOf(
        CROSS_GLASS,
        PETAL_SPIRAL,
        MEADOW_GLASS,
        BERRY_ZIGZAG
    )

    val FALLBACK_NAMES: List<String> = listOf(
        "CROSS GLASS",
        "PETAL SPIRAL",
        "MEADOW GLASS",
        "BERRY ZIGZAG",
        "CROSS GLASS II",
        "PETAL SPIRAL II",
        "MEADOW GLASS II",
        "BERRY ZIGZAG II",
        "CROSS GLASS III",
        "PETAL SPIRAL III",
        "MEADOW GLASS III",
        "BERRY ZIGZAG III"
    )

    val SCRAMBLE_DEPTHS: List<Int> = listOf(4, 6, 8)

    val MOVE_LIMITS: List<Int> = listOf(9, 12, 16)

    const val LEVEL_COUNT = 12

    fun patternFor(levelId: Int): List<String> = PATTERNS[(levelId - 1) % PATTERNS.size]

    fun depthFor(levelId: Int): Int = SCRAMBLE_DEPTHS[tierOf(levelId)]

    fun moveLimitFor(levelId: Int): Int = MOVE_LIMITS[tierOf(levelId)]

    private fun tierOf(levelId: Int): Int {
        val tier = (levelId - 1) / PATTERNS.size
        return if (tier < SCRAMBLE_DEPTHS.size) tier else SCRAMBLE_DEPTHS.size - 1
    }
}
