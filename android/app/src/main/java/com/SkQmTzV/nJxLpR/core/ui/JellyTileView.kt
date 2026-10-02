package com.SkQmTzV.nJxLpR.core.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.databinding.ViewJellyTileBinding
import com.SkQmTzV.nJxLpR.domain.model.JellyColor

class JellyTileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewJellyTileBinding.inflate(LayoutInflater.from(context), this)

    var row: Int = 0
        private set

    var col: Int = 0
        private set

    fun setPosition(rowIndex: Int, colIndex: Int) {
        row = rowIndex
        col = colIndex
    }

    fun bind(colour: JellyColor, fixed: Boolean, selected: Boolean, linked: Boolean) {
        binding.tileSprite.setImageResource(spriteOf(colour))
        binding.tileBase.background = buildBackground(colour, fixed, selected, linked)
        binding.tileGloss.alpha = if (linked) 0.9f else 0.6f

        val colourName = context.getString(labelRes(colour))
        contentDescription = context.getString(R.string.cd_tile, row + 1, col + 1, colourName)
        val stateRes = when {
            selected -> R.string.state_selected
            linked -> R.string.state_linked
            fixed -> R.string.state_fixed
            else -> R.string.state_free
        }
        ViewCompat.setStateDescription(this, context.getString(stateRes))
        isClickable = true
        isFocusable = true
    }

    fun glossView(): View = binding.tileGloss

    private fun buildBackground(
        colour: JellyColor,
        fixed: Boolean,
        selected: Boolean,
        linked: Boolean
    ): GradientDrawable {
        val template = when {
            selected -> R.drawable.bg_tile_selected
            fixed -> R.drawable.bg_tile_source
            linked -> R.drawable.bg_tile_linked
            else -> R.drawable.bg_tile_clay
        }
        val drawable = ContextCompat.getDrawable(context, template)?.mutate() as? GradientDrawable
            ?: GradientDrawable()
        drawable.cornerRadius = resources.getDimension(R.dimen.tile_radius)
        drawable.setColor(withAlpha(colour.argb, if (linked) FILL_LINKED else FILL_IDLE))
        val strokeWidth = resources.getDimensionPixelSize(
            if (selected || fixed || linked) R.dimen.board_stroke_thick else R.dimen.board_stroke
        )
        val strokeColour = when {
            selected -> Color.WHITE
            fixed -> ContextCompat.getColor(context, R.color.jelly_lemon)
            linked -> withAlpha(colour.argb, STROKE_FULL)
            else -> withAlpha(colour.argb, STROKE_IDLE)
        }
        drawable.setStroke(strokeWidth, strokeColour)
        return drawable
    }

    private fun withAlpha(argb: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(argb), Color.green(argb), Color.blue(argb))

    private fun spriteOf(colour: JellyColor): Int = when (colour) {
        JellyColor.PINK -> R.drawable.sprite_gem_pink
        JellyColor.LEMON -> R.drawable.sprite_gem_yellow
        JellyColor.MINT -> R.drawable.sprite_gem_mint
        JellyColor.BERRY -> R.drawable.sprite_gem_blue
        JellyColor.GRAPE -> R.drawable.sprite_gem_grape
    }

    private fun labelRes(colour: JellyColor): Int = when (colour) {
        JellyColor.PINK -> R.string.goal_pink
        JellyColor.LEMON -> R.string.goal_lemon
        JellyColor.MINT -> R.string.goal_mint
        JellyColor.BERRY -> R.string.goal_berry
        JellyColor.GRAPE -> R.string.goal_grape
    }

    companion object {
        private const val FILL_IDLE = 0x4D
        private const val FILL_LINKED = 0x8C
        private const val STROKE_IDLE = 0x8C
        private const val STROKE_FULL = 0xFF
    }
}
