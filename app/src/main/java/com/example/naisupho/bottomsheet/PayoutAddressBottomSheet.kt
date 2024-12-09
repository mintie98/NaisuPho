package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.BaseBottomSheetFragment
import com.example.naisupho.adapter.PayoutAddressAdapter
import com.example.naisupho.databinding.ActivityPayoutAddressBottomSheetBinding
import com.example.naisupho.model.Address
import com.example.naisupho.viewmodel.PayOutViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PayoutAddressBottomSheet(
    private val onAddressSelected: (Address) -> Unit,
    private val onAddNewAddress: () -> Unit // Callback khi bấm nút "Add New Address"
) : BaseBottomSheetFragment() {

    private var _binding: ActivityPayoutAddressBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PayOutViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityPayoutAddressBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Thiết lập RecyclerView và quan sát dữ liệu
        setupAddressList()

        // Xử lý nút "Add New Address"
        binding.btnAddNewAddress.setOnClickListener {
            Log.d("PayoutAddressBottomSheet", "Add New Address button clicked")
            dismiss() // Đóng BottomSheet hiện tại
            onAddNewAddress() // Mở AddressBottomSheet để thêm địa chỉ
        }
    }

    private fun setupAddressList() {
        viewModel.addressList.observe(viewLifecycleOwner) { addresses ->
            if (addresses.isNullOrEmpty()) {
                // Hiển thị thông báo nếu không có địa chỉ nào
                //Toast.makeText(context, "No addresses available", Toast.LENGTH_SHORT).show()
            } else {
                // Hiển thị danh sách địa chỉ trong RecyclerView
                Log.d("PayoutAddressBottomSheet", "Addresses received: $addresses")
                val adapter = PayoutAddressAdapter(addresses) { selectedAddress ->
                    onAddressSelected(selectedAddress) // Truyền địa chỉ đã chọn
                    dismiss() // Đóng BottomSheet
                }
                binding.rvAddressList.layoutManager = LinearLayoutManager(context)
                binding.rvAddressList.adapter = adapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Tránh memory leak
    }
}