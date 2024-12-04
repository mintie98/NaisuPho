package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.adapter.PayoutAddressAdapter
import com.example.naisupho.databinding.ActivityPayoutAddressBottomSheetBinding
import com.example.naisupho.model.Address
import com.example.naisupho.viewmodel.PayOutViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PayoutAddressBottomSheet(
    private val onAddressSelected: (Address) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: ActivityPayoutAddressBottomSheetBinding
    private val viewModel: PayOutViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ActivityPayoutAddressBottomSheetBinding.inflate(inflater, container, false)

        // Quan sát danh sách địa chỉ từ ViewModel
        viewModel.addressList.observe(viewLifecycleOwner) { addresses ->
            if (addresses.isNullOrEmpty()) {
                Toast.makeText(context, "No addresses available", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("PayoutAddressBottomSheet", "Addresses received: $addresses")
                val adapter = PayoutAddressAdapter(addresses) { selectedAddress ->
                    onAddressSelected(selectedAddress) // Truyền đối tượng Address
                    dismiss() // Đóng BottomSheet
                }
                binding.rvAddressList.layoutManager = LinearLayoutManager(context)
                binding.rvAddressList.adapter = adapter
            }
        }

        return binding.root
    }
}