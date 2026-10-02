package com.SkQmTzV.nJxLpR.core.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.GridLayout
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.core.config.GameConfig
import com.SkQmTzV.nJxLpR.databinding.ViewJellyBoardBinding
import com.SkQmTzV.nJxLpR.domain.model.BoardState
import com.SkQmTzV.nJxLpR.domain.model.Cell
import com.SkQmTzV.nJxLpR.domain.model.JellyColor

class JellyBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewJellyBoardBinding.inflate(LayoutInflater.from(context), this)
    private val tiles = ArrayList<JellyTileView>(SIDE * SIDE)
    private val running = ArrayList<Animator>()

    private var tileSize = 0
    private var interactive = true
    private var board: BoardState? = null
    private var selected: Cell? = null
    private var highlighted: Set<Cell> = emptySet()

    var onTileTap: ((Cell) -> Unit)? = null

    private val framePx: Int
        get() = resources.getDimensionPixelSize(R.dimen.board_frame)

    private val gapPx: Int
        get() = resources.getDimensionPixelSize(R.dimen.tile_gap)

    private val maxBoardPx: Int
        get() = resources.getDimensionPixelSize(R.dimen.board_max)

    init {
        buildTiles()
    }

    private fun buildTiles() {
        binding.boardGrid.removeAllViews()
        tiles.clear()
        for (row in 0 until SIDE) {
            for (col in 0 until SIDE) {
                val tile = JellyTileView(context)
                tile.setPosition(row, col)
                tile.setOnClickListener { handleTap(tile) }
                binding.boardGrid.addView(tile)
                tiles.add(tile)
            }
        }
    }

    private fun handleTap(tile: JellyTileView) {
        if (!interactive) return
        onTileTap?.invoke(Cell(tile.row, tile.col))
    }

    fun setInteractive(value: Boolean) {
        interactive = value
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availableHeight = MeasureSpec.getSize(heightMeasureSpec)
        var limit = if (availableWidth > 0) availableWidth else maxBoardPx
        if (availableHeight in 1 until limit) limit = availableHeight
        if (limit > maxBoardPx) limit = maxBoardPx

        val frame = framePx
        var tile = (limit - 2 * frame) / SIDE
        if (tile < MIN_TILE_PX) tile = MIN_TILE_PX
        val side = tile * SIDE + 2 * frame

        if (tile != tileSize) {
            tileSize = tile
            applyTileMetrics()
        }

        val spec = MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY)
        super.onMeasure(spec, spec)
        setMeasuredDimension(side, side)
    }

    private fun applyTileMetrics() {
        val gap = gapPx
        val inner = tileSize - 2 * gap
        for ((index, tile) in tiles.withIndex()) {
            val params = GridLayout.LayoutParams()
            params.width = if (inner > 0) inner else tileSize
            params.height = params.width
            params.setMargins(gap, gap, gap, gap)
            params.rowSpec = GridLayout.spec(index / SIDE)
            params.columnSpec = GridLayout.spec(index % SIDE)
            tile.layoutParams = params
        }
        val flowerParams = binding.boardFlower.layoutParams as LayoutParams
        flowerParams.width = tileSize * 2
        flowerParams.height = tileSize * 2
        binding.boardFlower.layoutParams = flowerParams
    }

    fun bind(state: BoardState) {
        board = state
        for (tile in tiles) {
            val cell = Cell(tile.row, tile.col)
            val colour = state.colorAt(cell)
            val linked = state.linked.contains(colour) && isOnLinkedPath(state, cell, colour)
            tile.bind(
                colour = colour,
                fixed = state.isFixed(cell),
                selected = selected == cell,
                linked = linked
            )
            tile.alpha = if (highlighted.isEmpty() || highlighted.contains(cell)) 1f else 0.82f
        }
    }

    private fun isOnLinkedPath(state: BoardState, cell: Cell, colour: JellyColor): Boolean {
        if (!colour.goal) return false
        return state.colorAt(cell) == colour && state.linked.contains(colour)
    }

    fun setSelected(cell: Cell?) {
        selected = cell
        board?.let { bind(it) }
    }

    fun setHighlight(cells: Set<Cell>) {
        highlighted = cells
        board?.let { bind(it) }
    }

    fun tileAt(cell: Cell): JellyTileView? =
        tiles.firstOrNull { it.row == cell.row && it.col == cell.col }

    fun animateSwapIn(first: Cell, second: Cell) {
        val tileA = tileAt(first) ?: return
        val tileB = tileAt(second) ?: return
        val step = tileSize.toFloat()
        val dx = (second.col - first.col) * step
        val dy = (second.row - first.row) * step

        tileA.translationX = dx
        tileA.translationY = dy
        tileB.translationX = -dx
        tileB.translationY = -dy

        val set = AnimatorSet()
        set.playTogether(
            ObjectAnimator.ofFloat(tileA, View.TRANSLATION_X, 0f),
            ObjectAnimator.ofFloat(tileA, View.TRANSLATION_Y, 0f),
            ObjectAnimator.ofFloat(tileB, View.TRANSLATION_X, 0f),
            ObjectAnimator.ofFloat(tileB, View.TRANSLATION_Y, 0f)
        )
        set.duration = GameConfig.SWAP_ANIM_MS
        set.interpolator = OvershootInterpolator(1.4f)
        track(set)
        set.start()
    }

    fun animateReject(cell: Cell) {
        val tile = tileAt(cell) ?: return
        val shift = resources.getDimension(R.dimen.space_xs)
        val animator = ObjectAnimator.ofFloat(tile, View.TRANSLATION_X, 0f, shift)
        animator.duration = GameConfig.SHAKE_ANIM_MS
        animator.interpolator = CycleInterpolator(3f)
        track(animator)
        animator.start()
    }

    fun animatePulse(cells: Set<Cell>) {
        for (cell in cells) {
            val tile = tileAt(cell) ?: continue
            val animator = ObjectAnimator.ofFloat(tile, View.ALPHA, 1f, 0.45f, 1f)
            animator.duration = GameConfig.HINT_PULSE_MS
            animator.repeatCount = 2
            track(animator)
            animator.start()
        }
    }

    fun animateLinkWave(cells: List<Cell>) {
        for ((index, cell) in cells.withIndex()) {
            val tile = tileAt(cell) ?: continue
            val set = AnimatorSet()
            set.playTogether(
                ObjectAnimator.ofFloat(tile, View.SCALE_X, 1f, 1.22f, 1f),
                ObjectAnimator.ofFloat(tile, View.SCALE_Y, 1f, 1.22f, 1f)
            )
            set.duration = GameConfig.PATH_CELL_MS
            set.startDelay = index * GameConfig.PATH_WAVE_STEP_MS
            track(set)
            set.start()
        }
    }

    fun cancelAnimations() {
        for (animator in ArrayList(running)) {
            animator.cancel()
        }
        running.clear()
        for (tile in tiles) {
            tile.translationX = 0f
            tile.translationY = 0f
            tile.scaleX = 1f
            tile.scaleY = 1f
            tile.alpha = 1f
        }
    }

    private fun track(animator: Animator) {
        running.add(animator)
    }

    override fun onDetachedFromWindow() {
        cancelAnimations()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val SIDE = GameConfig.BOARD_SIZE
        private const val MIN_TILE_PX = 24
    }
}
