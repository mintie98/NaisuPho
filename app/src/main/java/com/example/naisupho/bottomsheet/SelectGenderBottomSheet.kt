package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.naisupho.BaseBottomSheetFragment
import com.example.naisupho.databinding.ActivitySelectGenderBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectGenderBottomSheet : BaseBottomSheetFragment() {

    private var _binding: ActivitySelectGenderBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var listener: SelectGenderListener? = null

    interface SelectGenderListener {
        fun onGenderSelected(gender: String)
    }

    fun setListener(listener: SelectGenderListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySelectGenderBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.genderFemale.setOnClickListener {
            listener?.onGenderSelected("Female")
            dismiss()
        }

        binding.genderMale.setOnClickListener {
            listener?.onGenderSelected("Male")
            dismiss()
        }

        binding.genderNotSay.setOnClickListener {
            listener?.onGenderSelected("Other")
            dismiss()
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}