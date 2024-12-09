package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.naisupho.BaseBottomSheetFragment
import com.example.naisupho.databinding.BottomSheetAddressBinding
import com.example.naisupho.model.Address
import com.example.naisupho.viewmodel.AddressViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressBottomSheet(
    private val onSave: (Address) -> Unit,
    private val existingAddress: Address? = null
) : BaseBottomSheetFragment() {

    private val addressViewModel: AddressViewModel by viewModels()
    private var _binding: BottomSheetAddressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Quan sát để điền địa chỉ tự động
        addressViewModel.autoFilledAddress.observe(viewLifecycleOwner) { autoFilledAddress ->
            binding.autoFillTextView.text = Editable.Factory.getInstance().newEditable(autoFilledAddress)
        }

        // Xử lý nút tìm kiếm postcode
        binding.searchButton.setOnClickListener {
            val postcode = binding.postcodeEditText.text.toString()
            if (postcode.isNotEmpty()) {
                addressViewModel.fetchAddressByPostcode(postcode)
            }
        }

        // Thiết lập giá trị nếu đang chỉnh sửa
        existingAddress?.let { address ->
            binding.autoFillTextView.text = Editable.Factory.getInstance().newEditable(address.address1)
            binding.addressLineEditText.text = Editable.Factory.getInstance().newEditable(address.address2)
            binding.postcodeEditText.setText(address.postcode)
            binding.defaultCheckBox.isChecked = address.default
        }

        // Xử lý nút lưu
        binding.saveButton.setOnClickListener {
            val address1 = binding.autoFillTextView.text.toString()  // Địa chỉ tự động từ postcode
            val address2 = binding.addressLineEditText.text.toString()  // Địa chỉ thêm do người dùng nhập
            val postcode = binding.postcodeEditText.text.toString()
            val isDefault = binding.defaultCheckBox.isChecked

            // Tạo đối tượng Address mới hoặc cập nhật
            val address = Address(
                id = existingAddress?.id ?: "",  // Sử dụng id hiện có khi chỉnh sửa
                address1 = address1,
                address2 = address2,
                postcode = postcode,
                default = isDefault
            )

            onSave(address)  // Gọi hàm onSave để lưu địa chỉ
            dismiss()  // Đóng BottomSheet
        }

        // Nút hủy
        binding.cancelButton.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}