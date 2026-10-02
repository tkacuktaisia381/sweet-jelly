package com.SkQmTzV.nJxLpR.presentation.gameover

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.core.config.GameConfig
import com.SkQmTzV.nJxLpR.core.di.ServiceLocator
import com.SkQmTzV.nJxLpR.core.di.ViewModelFactory
import com.SkQmTzV.nJxLpR.core.navigation.Navigator
import com.SkQmTzV.nJxLpR.databinding.FragmentGameOverBinding
import com.SkQmTzV.nJxLpR.domain.model.JellyColor
import com.SkQmTzV.nJxLpR.domain.model.RoundResult
import com.SkQmTzV.nJxLpR.presentation.dialog.PatternPickerDialog
import kotlin.random.Random
import kotlinx.coroutines.launch

class GameOverFragment : Fragment() {

    private var binding: FragmentGameOverBinding? = null
    private val animators = ArrayList<Animator>()
    private var celebrationTask: Runnable? = null
    private var navigating = false

    private val viewModel: GameOverViewModel by viewModels {
        val context = requireContext().applicationContext
        ViewModelFactory {
            GameOverViewModel(
                outcome = readResult(),
                levelRepository = ServiceLocator.levelRepository(context)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameOverBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return

        bound.previewBoard.setInteractive(false)
        bound.previewBoard.bind(viewModel.previewBoard())

        bound.btnNext.setOnClickListener { goNext() }
        bound.btnAgain.setOnClickListener { goAgain() }
        bound.btnResultPatterns.setOnClickListener { openPicker() }
        bound.btnMenu.setOnClickListener { goMenu() }

        parentFragmentManager.setFragmentResultListener(
            PatternPickerDialog.RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val chosen = result.getInt(PatternPickerDialog.RESULT_LEVEL, 0)
            if (chosen > 0) restartAt(chosen)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { outcome -> render(outcome) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigating = false
    }

    private fun render(outcome: RoundResult) {
        val bound = binding ?: return
        bound.root.setBackgroundResource(
            if (outcome.win) R.drawable.bg_result_gradient else R.drawable.bg_result_gradient_lose
        )
        bound.txtVerdict.setText(if (outcome.win) R.string.result_win else R.string.result_lose)
        bound.txtResultLevel.text =
            getString(R.string.game_level_title, outcome.levelId, outcome.levelName)

        bindStars(outcome.stars)
        bindStats(outcome)

        bound.btnNext.visibility = if (viewModel.canAdvance()) View.VISIBLE else View.GONE
        bound.btnAgain.alpha = 1f

        if (outcome.win) scheduleCelebration()
        playStarPops(outcome.stars)
    }

    private fun bindStars(stars: Int) {
        val bound = binding ?: return
        val slots = listOf(bound.starOne, bound.starTwo, bound.starThree)
        for ((index, star) in slots.withIndex()) {
            val earned = index < stars
            star.alpha = if (earned) 1f else 0.22f
            if (earned) {
                star.clearColorFilter()
            } else {
                star.setColorFilter(DIM_FILTER, PorterDuff.Mode.SRC_ATOP)
            }
        }
        bound.starsRow.contentDescription = getString(R.string.cd_stars, stars)
    }

    private fun bindStats(outcome: RoundResult) {
        val bound = binding ?: return
        val lemon = ContextCompat.getColor(requireContext(), R.color.jelly_lemon)
        val pink = ContextCompat.getColor(requireContext(), R.color.jelly_pink)
        val mint = ContextCompat.getColor(requireContext(), R.color.jelly_mint)

        bound.resultStatOne.bind(
            outcome.score.toString(),
            getString(R.string.result_label_score),
            lemon
        )
        bound.resultStatTwo.bind(
            outcome.movesLeft.toString(),
            getString(R.string.result_label_moves),
            pink
        )
        bound.resultStatThree.bind(
            getString(R.string.result_linked_value, outcome.linkedCount),
            getString(R.string.result_label_linked),
            mint
        )
        bound.resultStatTwo.visibility = if (outcome.movesLeft > 0) View.VISIBLE else View.INVISIBLE
    }

    private fun playStarPops(stars: Int) {
        val bound = binding ?: return
        val slots = listOf(bound.starOne, bound.starTwo, bound.starThree)
        for (index in 0 until stars) {
            if (index >= slots.size) break
            val star = slots[index]
            val set = AnimatorSet()
            set.playTogether(
                ObjectAnimator.ofFloat(star, View.SCALE_X, 0.4f, 1.15f, 1f),
                ObjectAnimator.ofFloat(star, View.SCALE_Y, 0.4f, 1.15f, 1f)
            )
            set.duration = GameConfig.STAR_POP_MS
            set.startDelay = index * GameConfig.STAR_POP_STAGGER_MS
            set.interpolator = OvershootInterpolator(1.1f)
            register(set)
            set.start()
        }
    }

    private fun scheduleCelebration() {
        val bound = binding ?: return
        if (celebrationTask != null) return
        val task = Runnable {
            try {
                if (!isAdded || binding == null) return@Runnable
                spawnCelebration()
            } catch (e: Exception) {
                cancelAnimations()
            }
        }
        celebrationTask = task
        bound.root.postDelayed(task, CELEBRATION_DELAY_MS)
    }

    private fun spawnCelebration() {
        val bound = binding ?: return
        val layer = bound.celebrationLayer
        val width = layer.width
        val height = layer.height
        if (width <= 0 || height <= 0) return
        val random = Random(CELEBRATION_SEED)
        val density = resources.displayMetrics.density

        for (index in 0 until GameConfig.CELEBRATION_COUNT) {
            val size = ((PARTICLE_MIN_DP + random.nextInt(PARTICLE_RANGE_DP)) * density).toInt()
            val particle = ImageView(requireContext())
            particle.setImageResource(spriteFor(random.nextInt(JellyColor.GOALS.size)))
            particle.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            val params = FrameLayout.LayoutParams(size, size)
            params.leftMargin = random.nextInt(if (width > size) width - size else 1)
            layer.addView(particle, params)

            particle.translationY = -size.toFloat()
            val set = AnimatorSet()
            set.playTogether(
                ObjectAnimator.ofFloat(
                    particle,
                    View.TRANSLATION_Y,
                    -size.toFloat(),
                    height.toFloat()
                ),
                ObjectAnimator.ofFloat(
                    particle,
                    View.ROTATION,
                    0f,
                    (random.nextInt(ROTATION_RANGE) - ROTATION_HALF).toFloat()
                ),
                ObjectAnimator.ofFloat(particle, View.ALPHA, 1f, 0.2f)
            )
            set.duration = (FALL_MIN_MS + random.nextInt(FALL_RANGE_MS)).toLong()
            set.interpolator = AccelerateInterpolator()
            register(set)
            set.start()
        }
    }

    private fun spriteFor(index: Int): Int = when (index) {
        0 -> R.drawable.sprite_gem_pink
        1 -> R.drawable.sprite_gem_yellow
        2 -> R.drawable.sprite_gem_mint
        else -> R.drawable.sprite_gem_blue
    }

    private fun goNext() {
        restartAt(viewModel.nextLevelId())
    }

    private fun goAgain() {
        restartAt(viewModel.state.value.levelId)
    }

    private fun restartAt(levelId: Int) {
        try {
            if (navigating || !isAdded) return
            navigating = true
            cancelAnimations()
            Navigator.restartAt(parentFragmentManager, levelId)
        } catch (e: Exception) {
            navigating = false
        }
    }

    private fun goMenu() {
        try {
            if (navigating || !isAdded) return
            navigating = true
            cancelAnimations()
            Navigator.popToMenu(parentFragmentManager)
        } catch (e: Exception) {
            navigating = false
        }
    }

    private fun openPicker() {
        if (!isAdded) return
        PatternPickerDialog().show(parentFragmentManager, PatternPickerDialog.TAG)
    }

    private fun register(animator: Animator) {
        animators.add(animator)
    }

    private fun cancelAnimations() {
        for (animator in ArrayList(animators)) {
            animator.cancel()
        }
        animators.clear()
    }

    override fun onDestroyView() {
        cancelAnimations()
        celebrationTask?.let { binding?.root?.removeCallbacks(it) }
        celebrationTask = null
        binding?.previewBoard?.cancelAnimations()
        binding = null
        super.onDestroyView()
    }

    private fun readResult(): RoundResult {
        val args = arguments ?: Bundle()
        val ordinals = args.getIntArray(ARG_GRID) ?: IntArray(0)
        val palette = JellyColor.values()
        val grid = ArrayList<JellyColor>(ordinals.size)
        for (value in ordinals) {
            grid.add(if (value in palette.indices) palette[value] else JellyColor.GRAPE)
        }
        while (grid.size < BOARD_CELLS) {
            grid.add(JellyColor.GRAPE)
        }
        return RoundResult(
            levelId = args.getInt(ARG_LEVEL_ID, 1),
            levelName = args.getString(ARG_LEVEL_NAME).orEmpty(),
            win = args.getBoolean(ARG_WIN, false),
            score = args.getInt(ARG_SCORE, 0),
            stars = args.getInt(ARG_STARS, 0),
            movesLeft = args.getInt(ARG_MOVES_LEFT, 0),
            linkedCount = args.getInt(ARG_LINKED, 0),
            hasNext = args.getBoolean(ARG_HAS_NEXT, false),
            finalGrid = grid
        )
    }

    companion object {
        private const val ARG_LEVEL_ID = "arg_level_id"
        private const val ARG_LEVEL_NAME = "arg_level_name"
        private const val ARG_WIN = "arg_win"
        private const val ARG_SCORE = "arg_score"
        private const val ARG_STARS = "arg_stars"
        private const val ARG_MOVES_LEFT = "arg_moves_left"
        private const val ARG_LINKED = "arg_linked"
        private const val ARG_HAS_NEXT = "arg_has_next"
        private const val ARG_GRID = "arg_grid"

        private const val BOARD_CELLS = 36
        private const val DIM_FILTER = 0x66FFFFFF
        private const val CELEBRATION_DELAY_MS = 180L
        private const val CELEBRATION_SEED = 4242L
        private const val PARTICLE_MIN_DP = 18
        private const val PARTICLE_RANGE_DP = 11
        private const val FALL_MIN_MS = 1400
        private const val FALL_RANGE_MS = 800
        private const val ROTATION_RANGE = 440
        private const val ROTATION_HALF = 220

        fun newInstance(result: RoundResult): GameOverFragment {
            val fragment = GameOverFragment()
            val args = Bundle()
            args.putInt(ARG_LEVEL_ID, result.levelId)
            args.putString(ARG_LEVEL_NAME, result.levelName)
            args.putBoolean(ARG_WIN, result.win)
            args.putInt(ARG_SCORE, result.score)
            args.putInt(ARG_STARS, result.stars)
            args.putInt(ARG_MOVES_LEFT, result.movesLeft)
            args.putInt(ARG_LINKED, result.linkedCount)
            args.putBoolean(ARG_HAS_NEXT, result.hasNext)
            args.putIntArray(ARG_GRID, result.finalGrid.map { it.ordinal }.toIntArray())
            fragment.arguments = args
            return fragment
        }
    }
}
