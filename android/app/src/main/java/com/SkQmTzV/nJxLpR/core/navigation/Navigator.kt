package com.SkQmTzV.nJxLpR.core.navigation

import androidx.fragment.app.FragmentManager
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.domain.model.RoundResult
import com.SkQmTzV.nJxLpR.presentation.game.GameFragment
import com.SkQmTzV.nJxLpR.presentation.gameover.GameOverFragment
import com.SkQmTzV.nJxLpR.presentation.menu.MenuFragment
import com.SkQmTzV.nJxLpR.presentation.splash.SplashFragment

object Navigator {

    const val TAG_SPLASH = "splash"
    const val TAG_MENU = "menu"
    const val TAG_GAME = "game"
    const val TAG_RESULT = "result"

    fun showSplash(manager: FragmentManager) {
        manager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, SplashFragment(), TAG_SPLASH)
            .commitAllowingStateLoss()
    }

    fun showMenu(manager: FragmentManager) {
        manager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment_container, MenuFragment(), TAG_MENU)
            .commitAllowingStateLoss()
    }

    fun showGame(manager: FragmentManager, levelId: Int) {
        manager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_up,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_down
            )
            .replace(R.id.fragment_container, GameFragment.newInstance(levelId), TAG_GAME)
            .addToBackStack(TAG_GAME)
            .commitAllowingStateLoss()
    }

    fun showResult(manager: FragmentManager, result: RoundResult) {
        manager.beginTransaction()
            .setCustomAnimations(
                R.anim.scale_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.scale_out
            )
            .replace(R.id.fragment_container, GameOverFragment.newInstance(result), TAG_RESULT)
            .addToBackStack(TAG_RESULT)
            .commitAllowingStateLoss()
    }

    fun restartAt(manager: FragmentManager, levelId: Int) {
        popToMenu(manager)
        showGame(manager, levelId)
    }

    fun popToMenu(manager: FragmentManager) {
        if (manager.backStackEntryCount > 0) {
            manager.popBackStack(TAG_GAME, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun back(manager: FragmentManager) {
        if (manager.backStackEntryCount > 0) manager.popBackStack()
    }
}
