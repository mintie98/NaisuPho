package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.naisupho.databinding.FragmentPaymentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit  var binding: FragmentPaymentBottomSheetBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBottomSheetBinding.inflate(inflater, container, false)
        binding.closeIcon.setOnClickListener {
            dismiss()
        }
        binding.addCardButton.setOnClickListener {
            addCart()
        }

        return binding.root
    }

    private fun addCart() {
        TODO("Not yet implemented")
    }
}