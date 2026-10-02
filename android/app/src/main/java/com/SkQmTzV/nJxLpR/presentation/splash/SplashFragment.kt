package com.SkQmTzV.nJxLpR.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.SkQmTzV.nJxLpR.core.navigation.Navigator
import com.SkQmTzV.nJxLpR.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var animator: SplashAnimator? = null
    private var startTask: Runnable? = null
    private var navigated = false

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentSplashBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.splashProgress.isIndeterminate = true

        val splashAnimator = SplashAnimator(bound)
        animator = splashAnimator
        val task = Runnable {
            try {
                if (!isAdded || binding == null) return@Runnable
                splashAnimator.start()
            } catch (e: Exception) {
                animator?.cancel()
            }
        }
        startTask = task
        bound.root.post(task)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ready.collect { ready ->
                    if (ready) goToMenu()
                }
            }
        }
    }

    private fun goToMenu() {
        try {
            if (navigated || !isAdded || binding == null) return
            navigated = true
            viewModel.consume()
            animator?.cancel()
            Navigator.showMenu(parentFragmentManager)
        } catch (e: Exception) {
            navigated = false
        }
    }

    override fun onDestroyView() {
        animator?.cancel()
        animator = null
        startTask?.let { binding?.root?.removeCallbacks(it) }
        startTask = null
        binding = null
        super.onDestroyView()
    }
}
