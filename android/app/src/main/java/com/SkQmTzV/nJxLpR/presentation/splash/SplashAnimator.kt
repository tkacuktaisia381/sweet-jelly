package com.SkQmTzV.nJxLpR.presentation.splash

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import com.SkQmTzV.nJxLpR.core.config.GameConfig
import com.SkQmTzV.nJxLpR.databinding.FragmentSplashBinding

class SplashAnimator(private val binding: FragmentSplashBinding) {

    private val animators = ArrayList<Animator>()

    fun start() {
        cancel()
        prepare()
        playTitleEntry()
        playSubtitleEntry()
        playFrameBreathing()
        playGemPulses()
        playLoadingBlink()
    }

    private fun prepare() {
        binding.txtSplashTitle.alpha = 0f
        binding.txtSplashTitle.translationY = TITLE_RISE_DP * binding.root.resources.displayMetrics.density
        binding.txtSplashSubtitle.alpha = 0f
        binding.txtLoading.alpha = LOADING_MIN
    }

    private fun playTitleEntry() {
        val set = AnimatorSet()
        set.playTogether(
            ObjectAnimator.ofFloat(binding.txtSplashTitle, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(binding.txtSplashTitle, View.TRANSLATION_Y, 0f)
        )
        set.duration = GameConfig.TITLE_RISE_MS
        set.startDelay = TITLE_DELAY_MS
        set.interpolator = DecelerateInterpolator()
        register(set)
        set.start()
    }

    private fun playSubtitleEntry() {
        val fade = ObjectAnimator.ofFloat(binding.txtSplashSubtitle, View.ALPHA, 0f, 1f)
        fade.duration = GameConfig.SUBTITLE_FADE_MS
        fade.startDelay = SUBTITLE_DELAY_MS
        fade.interpolator = DecelerateInterpolator()
        register(fade)
        fade.start()
    }

    private fun playFrameBreathing() {
        breathe(binding.imgFrame, BREATHE_SCALE)
        breathe(binding.imgFlower, BREATHE_SCALE_FLOWER)
    }

    private fun breathe(target: View, peak: Float) {
        val set = AnimatorSet()
        val scaleX = ObjectAnimator.ofFloat(target, View.SCALE_X, 1f, peak)
        val scaleY = ObjectAnimator.ofFloat(target, View.SCALE_Y, 1f, peak)
        for (animator in listOf(scaleX, scaleY)) {
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
        }
        set.playTogether(scaleX, scaleY)
        set.duration = GameConfig.BREATHE_MS
        set.interpolator = AccelerateDecelerateInterpolator()
        register(set)
        set.start()
    }

    private fun playGemPulses() {
        val gems: List<ImageView> = listOf(
            binding.gemPink,
            binding.gemLemon,
            binding.gemBerry,
            binding.gemMint
        )
        for ((index, gem) in gems.withIndex()) {
            val scaleX = ObjectAnimator.ofFloat(gem, View.SCALE_X, 1f, GEM_PEAK)
            val scaleY = ObjectAnimator.ofFloat(gem, View.SCALE_Y, 1f, GEM_PEAK)
            for (animator in listOf(scaleX, scaleY)) {
                animator.repeatCount = ValueAnimator.INFINITE
                animator.repeatMode = ValueAnimator.REVERSE
            }
            val set = AnimatorSet()
            set.playTogether(scaleX, scaleY)
            set.duration = GameConfig.GEM_PULSE_MS
            set.startDelay = index * GameConfig.GEM_PULSE_STAGGER_MS
            set.interpolator = AccelerateDecelerateInterpolator()
            register(set)
            set.start()
        }
    }

    private fun playLoadingBlink() {
        val blink = ObjectAnimator.ofFloat(binding.txtLoading, View.ALPHA, LOADING_MIN, 1f)
        blink.duration = GameConfig.LOADING_BLINK_MS
        blink.repeatCount = ValueAnimator.INFINITE
        blink.repeatMode = ValueAnimator.REVERSE
        blink.interpolator = AccelerateDecelerateInterpolator()
        register(blink)
        blink.start()
    }

    private fun register(animator: Animator) {
        animators.add(animator)
    }

    fun cancel() {
        for (animator in ArrayList(animators)) {
            animator.cancel()
        }
        animators.clear()
    }

    companion object {
        private const val TITLE_DELAY_MS = 250L
        private const val SUBTITLE_DELAY_MS = 650L
        private const val TITLE_RISE_DP = 28f
        private const val BREATHE_SCALE = 1.04f
        private const val BREATHE_SCALE_FLOWER = 1.08f
        private const val GEM_PEAK = 1.18f
        private const val LOADING_MIN = 0.45f
    }
}
