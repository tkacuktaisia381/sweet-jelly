package com.SkQmTzV.nJxLpR.core.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.SkQmTzV.nJxLpR.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    fun bind(value: String, label: String, accent: Int) {
        binding.statValue.text = value
        binding.statValue.setTextColor(accent)
        binding.statLabel.text = label
        binding.statDot.background?.mutate()?.setTint(accent)
        val stroke = Color.argb(
            STROKE_ALPHA,
            Color.red(accent),
            Color.green(accent),
            Color.blue(accent)
        )
        binding.statCard.setStrokeColor(stroke)
        contentDescription = "$label $value"
    }

    companion object {
        private const val STROKE_ALPHA = 0x55
    }
}
