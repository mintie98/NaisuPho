package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.naisupho.databinding.ActivityAddPhoneBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddPhoneBottomSheet : BottomSheetDialogFragment() {

    private var _binding: ActivityAddPhoneBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var listener: AddPhoneListener? = null

    interface AddPhoneListener {
        fun onSend(countryCode: String, phoneNumber: String)
    }

    fun setListener(listener: AddPhoneListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityAddPhoneBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendButton.setOnClickListener {
            val countryCode = binding.countryCodeSpinner.selectedItem.toString()
            val phoneNumber = binding.phoneNumberEditText.text.toString()
            listener?.onSend(countryCode, phoneNumber)
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