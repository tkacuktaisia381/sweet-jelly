package com.SkQmTzV.nJxLpR.presentation.menu

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
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
import com.SkQmTzV.nJxLpR.databinding.FragmentMenuBinding
import com.SkQmTzV.nJxLpR.presentation.dialog.PatternPickerDialog
import com.SkQmTzV.nJxLpR.presentation.dialog.SettingsDialog
import com.SkQmTzV.nJxLpR.presentation.dialog.TutorialDialog
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var binding: FragmentMenuBinding? = null
    private val animators = ArrayList<Animator>()
    private var launched = false

    private val viewModel: MenuViewModel by viewModels {
        val context = requireContext().applicationContext
        ViewModelFactory {
            MenuViewModel(
                levelRepository = ServiceLocator.levelRepository(context),
                progressRepository = ServiceLocator.progressRepository(context)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentMenuBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return

        bound.btnPlay.setOnClickListener { startLevel() }
        bound.btnPatterns.setOnClickListener { openPicker() }
        bound.btnTutorial.setOnClickListener { openTutorial() }
        bound.btnSettings.setOnClickListener { openSettings() }

        parentFragmentManager.setFragmentResultListener(
            PatternPickerDialog.RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val chosen = result.getInt(PatternPickerDialog.RESULT_LEVEL, 0)
            if (chosen > 0) startLevel(chosen)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }

        playEntrance()
    }

    override fun onResume() {
        super.onResume()
        launched = false
        viewModel.refresh()
    }

    private fun render(state: MenuUiState) {
        val bound = binding ?: return
        val lemon = ContextCompat.getColor(requireContext(), R.color.jelly_lemon_deep)
        val pink = ContextCompat.getColor(requireContext(), R.color.jelly_pink_deep)
        val mint = ContextCompat.getColor(requireContext(), R.color.jelly_mint)

        if (state.fresh) {
            bound.statOne.visibility = View.VISIBLE
            bound.statTwo.visibility = View.VISIBLE
            bound.statThree.visibility = View.GONE
            bound.statOne.bind(
                getString(R.string.pattern_label, state.nextLevelId),
                getString(R.string.stat_label_next),
                lemon
            )
            bound.statTwo.bind(
                getString(R.string.stat_value_new),
                getString(R.string.stat_label_status),
                pink
            )
            bound.menuStatsRow.visibility = View.VISIBLE
            return
        }

        bound.statOne.visibility = View.VISIBLE
        bound.statTwo.visibility = View.VISIBLE
        bound.statThree.visibility = View.VISIBLE
        bound.statOne.bind(
            getString(R.string.stat_value_stars, state.totalStars, state.maxStars),
            getString(R.string.stat_label_stars),
            lemon
        )
        bound.statTwo.bind(
            getString(R.string.pattern_label, state.bestLevelId),
            getString(R.string.stat_label_best),
            pink
        )
        bound.statThree.bind(
            state.solvedCount.toString(),
            getString(R.string.stat_label_solved),
            mint
        )
        bound.menuStatsRow.visibility = View.VISIBLE
    }

    private fun playEntrance() {
        val bound = binding ?: return
        val density = resources.displayMetrics.density

        bound.cardSheet.alpha = 0f
        bound.cardSheet.translationY = SHEET_RISE_DP * density
        val entry = AnimatorSet()
        entry.playTogether(
            ObjectAnimator.ofFloat(bound.cardSheet, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(bound.cardSheet, View.TRANSLATION_Y, 0f)
        )
        entry.duration = GameConfig.SHEET_RISE_MS
        entry.interpolator = DecelerateInterpolator()
        register(entry)
        entry.start()

        val cards = listOf(bound.statOne, bound.statTwo, bound.statThree)
        for ((index, card) in cards.withIndex()) {
            card.alpha = 0f
            card.translationY = CHIP_RISE_DP * density
            val set = AnimatorSet()
            set.playTogether(
                ObjectAnimator.ofFloat(card, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(card, View.TRANSLATION_Y, 0f)
            )
            set.duration = CHIP_RISE_MS
            set.startDelay = GameConfig.SHEET_RISE_MS + index * GameConfig.CHIP_STAGGER_MS
            set.interpolator = DecelerateInterpolator()
            register(set)
            set.start()
        }

        breathe(bound.imgMenuFrame, BREATHE_FRAME)
        breathe(bound.imgMenuFlower, BREATHE_FLOWER)
        pulseCta()
    }

    private fun breathe(target: View, peak: Float) {
        val scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 1f, peak)
        val scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 1f, peak)
        val set = AnimatorSet()
        for (animator in listOf(scaleX, scaleY)) {
            animator.repeatCount = LOOP_CYCLES
            animator.repeatMode = ValueAnimator.REVERSE
        }
        set.playTogether(scaleX, scaleY)
        set.duration = GameConfig.BREATHE_MS
        set.interpolator = AccelerateDecelerateInterpolator()
        register(set)
        set.start()
    }

    private fun pulseCta() {
        val bound = binding ?: return
        val scaleX = ObjectAnimator.ofFloat(bound.btnPlay, View.SCALE_X, 1f, CTA_PEAK)
        val scaleY = ObjectAnimator.ofFloat(bound.btnPlay, View.SCALE_Y, 1f, CTA_PEAK)
        val set = AnimatorSet()
        for (animator in listOf(scaleX, scaleY)) {
            animator.repeatCount = LOOP_CYCLES
            animator.repeatMode = ValueAnimator.REVERSE
        }
        set.playTogether(scaleX, scaleY)
        set.duration = GameConfig.CTA_PULSE_MS
        set.startDelay = GameConfig.SHEET_RISE_MS
        set.interpolator = AccelerateDecelerateInterpolator()
        register(set)
        set.start()
    }

    private fun startLevel(levelId: Int = 0) {
        try {
            if (launched || !isAdded || binding == null) return
            launched = true
            val target = if (levelId > 0) levelId else viewModel.state.value.nextLevelId
            cancelAnimations()
            Navigator.showGame(parentFragmentManager, target)
        } catch (e: Exception) {
            launched = false
        }
    }

    private fun openPicker() {
        if (!isAdded) return
        PatternPickerDialog().show(parentFragmentManager, PatternPickerDialog.TAG)
    }

    private fun openTutorial() {
        if (!isAdded) return
        viewModel.markTutorialSeen()
        TutorialDialog().show(parentFragmentManager, TutorialDialog.TAG)
    }

    private fun openSettings() {
        if (!isAdded) return
        SettingsDialog().show(parentFragmentManager, SettingsDialog.TAG)
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
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val SHEET_RISE_DP = 120f
        private const val CHIP_RISE_DP = 16f
        private const val CHIP_RISE_MS = 260L
        private const val BREATHE_FRAME = 1.04f
        private const val BREATHE_FLOWER = 1.07f
        private const val CTA_PEAK = 1.03f
        private const val LOOP_CYCLES = 5
    }
}
