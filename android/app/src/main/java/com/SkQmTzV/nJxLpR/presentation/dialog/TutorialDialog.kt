package com.SkQmTzV.nJxLpR.presentation.dialog

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.DialogFragment
import com.SkQmTzV.nJxLpR.databinding.DialogTutorialBinding

class TutorialDialog : DialogFragment() {

    private var binding: DialogTutorialBinding? = null
    private val animators = ArrayList<Animator>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogTutorialBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.btnTutorialClose.setOnClickListener { dismissAllowingStateLoss() }
        playSwapLoop()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun playSwapLoop() {
        val bound = binding ?: return
        val shift = SHIFT_DP * resources.displayMetrics.density
        val left = ObjectAnimator.ofFloat(
            bound.tutorialGemLeft,
            View.TRANSLATION_X,
            -shift,
            shift
        )
        val right = ObjectAnimator.ofFloat(
            bound.tutorialGemRight,
            View.TRANSLATION_X,
            shift,
            -shift
        )
        for (animator in listOf(left, right)) {
            animator.repeatCount = CYCLES
            animator.repeatMode = ValueAnimator.REVERSE
        }
        val set = AnimatorSet()
        set.playTogether(left, right)
        set.duration = SWAP_MS
        set.interpolator = AccelerateDecelerateInterpolator()
        animators.add(set)
        set.start()
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
        const val TAG = "tutorial_dialog"
        private const val SHIFT_DP = 28f
        private const val SWAP_MS = 900L
        private const val CYCLES = 9
    }
}
