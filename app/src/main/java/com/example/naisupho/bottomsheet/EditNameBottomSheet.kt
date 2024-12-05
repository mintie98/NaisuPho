package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.naisupho.databinding.ActivityEditNameBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditNameBottomSheet : BottomSheetDialogFragment() {

    private var _binding: ActivityEditNameBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var listener: EditNameListener? = null

    interface EditNameListener {
        fun onSave(name: String)
    }

    fun setListener(listener: EditNameListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityEditNameBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveButton.setOnClickListener {
            val name = binding.editName.text.toString()
            listener?.onSave(name)
            dismiss()
        }

        binding.closeIcon.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}