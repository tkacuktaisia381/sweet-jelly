package com.SkQmTzV.nJxLpR.core.ui

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.databinding.ViewMoveMeterBinding

class MoveMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMoveMeterBinding.inflate(LayoutInflater.from(context), this)
    private var compact = false
    private var pulse: ObjectAnimator? = null
    private var lastLeft = -1

    init {
        orientation = VERTICAL
    }

    fun setCompact(value: Boolean) {
        compact = value
        binding.meterCompactText.visibility = if (value) View.VISIBLE else View.GONE
        binding.meterRow.visibility = if (value) View.GONE else View.VISIBLE
        binding.meterBar.visibility = if (value) View.GONE else View.VISIBLE
    }

    fun bind(left: Int, limit: Int) {
        val safeLimit = if (limit <= 0) 1 else limit
        val low = left <= LOW_THRESHOLD
        val accent = ContextCompat.getColor(
            context,
            if (low) R.color.jelly_pink_deep else R.color.jelly_pink
        )

        binding.meterCompactText.text = context.getString(R.string.game_moves_compact, left, safeLimit)
        binding.meterCompactText.setTextColor(
            ContextCompat.getColor(
                context,
                if (low) R.color.jelly_pink_deep else R.color.jelly_lemon
            )
        )
        binding.meterValue.text = left.toString()
        binding.meterValue.setTextColor(
            ContextCompat.getColor(
                context,
                if (low) R.color.jelly_pink_deep else R.color.grape_500
            )
        )
        binding.meterBar.setIndicatorColor(accent)
        binding.meterBar.progress = (left * PERCENT / safeLimit).coerceIn(0, PERCENT)

        val description = context.getString(R.string.sd_moves, left)
        contentDescription = description
        ViewCompat.setStateDescription(this, description)

        if (lastLeft != -1 && lastLeft != left) playPulse()
        lastLeft = left
    }

    private fun playPulse() {
        val target: View = if (compact) binding.meterCompactText else binding.meterValue
        pulse?.cancel()
        val animator = ObjectAnimator.ofFloat(target, View.SCALE_X, 1f, 1.15f, 1f)
        animator.duration = PULSE_MS
        animator.start()
        pulse = animator
    }

    fun cancelAnimations() {
        pulse?.cancel()
        pulse = null
    }

    override fun onDetachedFromWindow() {
        cancelAnimations()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val LOW_THRESHOLD = 3
        private const val PERCENT = 100
        private const val PULSE_MS = 300L
    }
}
