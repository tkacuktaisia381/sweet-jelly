package com.SkQmTzV.nJxLpR.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.SkQmTzV.nJxLpR.R
import com.SkQmTzV.nJxLpR.core.di.ServiceLocator
import com.SkQmTzV.nJxLpR.databinding.DialogSettingsBinding
import com.SkQmTzV.nJxLpR.domain.repository.ProgressRepository

class SettingsDialog : DialogFragment() {

    private var binding: DialogSettingsBinding? = null
    private var repository: ProgressRepository? = null
    private var resetArmed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogSettingsBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        val store = ServiceLocator.progressRepository(requireContext().applicationContext)
        repository = store

        bound.switchSound.isChecked = store.soundEnabled()
        bound.switchVibration.isChecked = store.vibrationEnabled()

        bound.switchSound.setOnCheckedChangeListener { _, checked ->
            repository?.setSoundEnabled(checked)
        }
        bound.switchVibration.setOnCheckedChangeListener { _, checked ->
            repository?.setVibrationEnabled(checked)
        }
        bound.btnReset.setOnClickListener { onReset() }
        bound.btnSettingsClose.setOnClickListener { dismissAllowingStateLoss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun onReset() {
        val bound = binding ?: return
        if (!resetArmed) {
            resetArmed = true
            bound.btnReset.setText(R.string.settings_reset_confirm)
            return
        }
        repository?.resetProgress()
        resetArmed = false
        bound.btnReset.setText(R.string.settings_reset)
        dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        binding?.switchSound?.setOnCheckedChangeListener(null)
        binding?.switchVibration?.setOnCheckedChangeListener(null)
        binding = null
        repository = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "settings_dialog"
    }
}
