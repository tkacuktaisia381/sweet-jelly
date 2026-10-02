package com.SkQmTzV.nJxLpR.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.SkQmTzV.nJxLpR.core.di.ServiceLocator
import com.SkQmTzV.nJxLpR.databinding.DialogPatternPickerBinding

class PatternPickerDialog : DialogFragment() {

    private var binding: DialogPatternPickerBinding? = null
    private var adapter: PatternAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogPatternPickerBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        val context = requireContext().applicationContext

        val created = PatternAdapter { levelId -> pick(levelId) }
        adapter = created
        bound.pickerList.layoutManager = GridLayoutManager(requireContext(), COLUMNS)
        bound.pickerList.adapter = created
        created.submit(
            ServiceLocator.levelRepository(context).levels(),
            ServiceLocator.progressRepository(context).all()
        )

        bound.btnPickerClose.setOnClickListener { dismissAllowingStateLoss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun pick(levelId: Int) {
        try {
            if (!isAdded) return
            val payload = Bundle()
            payload.putInt(RESULT_LEVEL, levelId)
            parentFragmentManager.setFragmentResult(RESULT_KEY, payload)
            dismissAllowingStateLoss()
        } catch (e: Exception) {
            dismissAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        binding?.pickerList?.adapter = null
        adapter = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "pattern_picker"
        const val RESULT_KEY = "pattern_picker_result"
        const val RESULT_LEVEL = "pattern_picker_level"
        private const val COLUMNS = 3
    }
}
