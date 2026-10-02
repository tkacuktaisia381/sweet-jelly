package com.SkQmTzV.nJxLpR

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.SkQmTzV.nJxLpR.core.di.ServiceLocator
import com.SkQmTzV.nJxLpR.core.navigation.Navigator
import com.SkQmTzV.nJxLpR.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)

        val created = ActivityMainBinding.inflate(layoutInflater)
        binding = created
        setContentView(created.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    if (isFinishing || isDestroyed) return
                    if (supportFragmentManager.backStackEntryCount > 0) {
                        supportFragmentManager.popBackStack()
                    } else {
                        finish()
                    }
                } catch (e: Exception) {
                    finish()
                }
            }
        })

        if (savedInstanceState == null) {
            Navigator.showSplash(supportFragmentManager)
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}
