package com.SkQmTzV.nJxLpR.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.databinding.ViewGoalChipBinding
import com.SkQmTzV.nJxLpR.domain.model.JellyColor

class GoalChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewGoalChipBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
    }

    fun bind(colour: JellyColor, linked: Boolean) {
        val label = context.getString(labelRes(colour))
        binding.goalDot.background?.mutate()?.setTint(colour.argb)
        binding.goalName.text = label
        binding.goalName.alpha = if (linked) 1f else 0.72f
        binding.goalMark.text = context.getString(
            if (linked) R.string.goal_mark_linked else R.string.goal_mark_pending
        )
        binding.goalMark.setTextColor(
            ContextCompat.getColor(
                context,
                if (linked) R.color.jelly_mint else R.color.ink_faint
            )
        )
        val state = context.getString(
            if (linked) R.string.sd_goal_linked else R.string.sd_goal_pending,
            label
        )
        contentDescription = state
        ViewCompat.setStateDescription(this, state)
    }

    private fun labelRes(colour: JellyColor): Int = when (colour) {
        JellyColor.PINK -> R.string.goal_pink
        JellyColor.LEMON -> R.string.goal_lemon
        JellyColor.MINT -> R.string.goal_mint
        JellyColor.BERRY -> R.string.goal_berry
        JellyColor.GRAPE -> R.string.goal_grape
    }
}
