package com.SkQmTzV.nJxLpR.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SkQmTzV.nJxLpR.core.config.GameConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val mutableReady = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = mutableReady.asStateFlow()

    private var timer: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            mutableReady.value = true
        }
    }

    fun consume() {
        mutableReady.value = false
    }

    override fun onCleared() {
        timer?.cancel()
        timer = null
        super.onCleared()
    }
}
