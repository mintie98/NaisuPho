package com.example.naisupho.userInfo

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.BaseActivity
import com.example.naisupho.adapter.AddressAdapter
import com.example.naisupho.bottomsheet.AddressBottomSheet
import com.example.naisupho.databinding.ActivityAddressBinding
import com.example.naisupho.model.Address
import com.example.naisupho.viewmodel.AddressViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddressActivity : BaseActivity() {

    private lateinit var binding: ActivityAddressBinding
    private lateinit var addressAdapter: AddressAdapter
    private val addressViewModel: AddressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance().getReference("Users/$userId/name")
        userRef.get().addOnSuccessListener { snapshot ->
            val userName = snapshot.getValue(String::class.java) ?: "Unknown User"
            setupRecyclerView(userName)
            observeViewModel()
        }

        binding.backButton.setOnClickListener { finish() }

        binding.addNewAddressButton.setOnClickListener {
            val bottomSheet = AddressBottomSheet(
                onSave = { newAddress ->
                    addressViewModel.addAddress(newAddress)
                },
                existingAddress = null
            )
            bottomSheet.show(supportFragmentManager, "AddressBottomSheet")
        }
    }

    private fun setupRecyclerView(userName: String) {
        addressAdapter = AddressAdapter (userName){ address, action ->
            when (action) {
                "edit" -> editAddress(address)
                "delete" -> addressViewModel.deleteAddress(address.id)
            }
        }
        binding.addressRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.addressRecyclerView.adapter = addressAdapter
    }

    private fun observeViewModel() {
        addressViewModel.addresses.observe(this) { addresses ->
            addressAdapter.updateAddresses(addresses)
        }
    }
    private fun editAddress(address: Address) {
        val bottomSheet = AddressBottomSheet(
            onSave = { updatedAddress ->
                addressViewModel.updateAddress(updatedAddress)
            },
            existingAddress = address  // Truyền địa chỉ hiện tại để cập nhật
        )
        bottomSheet.show(supportFragmentManager, "AddressBottomSheet")
    }
}