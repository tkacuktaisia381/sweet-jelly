package com.SkQmTzV.nJxLpR.presentation.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.databinding.ItemPatternBinding
import com.SkQmTzV.nJxLpR.domain.model.JellyColor
import com.SkQmTzV.nJxLpR.domain.model.LevelDefinition
import com.SkQmTzV.nJxLpR.domain.model.LevelProgress

class PatternAdapter(
    private val onPick: (Int) -> Unit
) : RecyclerView.Adapter<PatternAdapter.PatternHolder>() {

    private val levels = ArrayList<LevelDefinition>()
    private val progress = ArrayList<LevelProgress>()

    fun submit(nextLevels: List<LevelDefinition>, nextProgress: List<LevelProgress>) {
        levels.clear()
        levels.addAll(nextLevels)
        progress.clear()
        progress.addAll(nextProgress)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatternHolder {
        val binding = ItemPatternBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatternHolder(binding)
    }

    override fun getItemCount(): Int = levels.size

    override fun onBindViewHolder(holder: PatternHolder, position: Int) {
        val level = levels[position]
        val state = progress.firstOrNull { it.levelId == level.id }
        holder.bind(level, state, onPick)
    }

    class PatternHolder(
        private val binding: ItemPatternBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(level: LevelDefinition, state: LevelProgress?, onPick: (Int) -> Unit) {
            val context = binding.root.context
            val unlocked = state?.unlocked ?: (level.id == 1)
            val stars = state?.bestStars ?: 0

            binding.patternTitle.text = context.getString(R.string.pattern_label, level.id)
            binding.patternDot.background?.mutate()?.setTint(accentFor(level.id))
            binding.patternLock.visibility = if (unlocked) View.GONE else View.VISIBLE
            binding.patternFrame.alpha = if (unlocked) 1f else 0.35f
            binding.root.alpha = if (unlocked) 1f else 0.55f
            binding.root.isEnabled = unlocked

            val slots = listOf(
                binding.patternStarOne,
                binding.patternStarTwo,
                binding.patternStarThree
            )
            for ((index, star) in slots.withIndex()) {
                star.alpha = if (index < stars) 1f else 0.2f
            }
            binding.patternStars.visibility = if (stars > 0) View.VISIBLE else View.INVISIBLE

            binding.root.contentDescription = if (unlocked) {
                context.getString(R.string.pattern_label, level.id)
            } else {
                context.getString(R.string.pattern_locked)
            }

            binding.root.setOnClickListener {
                if (unlocked) onPick(level.id)
            }
        }

        private fun accentFor(levelId: Int): Int {
            val palette = JellyColor.GOALS
            return palette[(levelId - 1) % palette.size].argb
        }
    }
}
