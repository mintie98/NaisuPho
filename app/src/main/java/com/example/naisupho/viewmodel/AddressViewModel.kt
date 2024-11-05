package com.example.naisupho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.naisupho.interfaces.ZipcloudApiService
import com.example.naisupho.interfaces.ZipcloudResponse
import com.example.naisupho.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    @Named("ZipcloudClient") private val zipcloudApiService: ZipcloudApiService
) : ViewModel() {

    private lateinit var databaseRef: DatabaseReference

    private val _addresses = MutableLiveData<List<Address>>()
    val addresses: LiveData<List<Address>> get() = _addresses

    private val _autoFilledAddress = MutableLiveData<String>()
    val autoFilledAddress: LiveData<String> get() = _autoFilledAddress

    init {
        initializeUser()
    }

    // Khởi tạo `userId` từ FirebaseAuth
    private fun initializeUser() {
        firebaseAuth.currentUser?.let { user ->
            val userId = user.uid
            databaseRef = firebaseDatabase.getReference("Users/$userId/addresses")
            fetchAddresses()  // Gọi hàm lấy danh sách địa chỉ
        } ?: run {
            Log.e("AddressViewModel", "User is not logged in.")
        }
    }

    private fun fetchAddresses() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val addressList = mutableListOf<Address>()
                for (child in snapshot.children) {
                    val address = child.getValue(Address::class.java)
                    Log.d("AddressViewModel", "Fetched address: ${address}")
                    if (address != null) {
                        addressList.add(address)
                    }
                }
                // Sắp xếp địa chỉ với địa chỉ default lên trên cùng
                _addresses.value = addressList.sortedByDescending { it.default }
                Log.d("AddressViewModel", "Total addresses fetched: ${addressList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddressViewModel", "Error fetching addresses: ${error.message}")
            }
        })
    }

    // Hàm dùng chung để xóa default của các địa chỉ khác
    private fun clearOtherDefaults(currentAddressId: String?) {
        databaseRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { child ->
                val address = child.getValue(Address::class.java)
                val addressId = child.key ?: return@forEach
                // Đặt default = false cho tất cả địa chỉ khác địa chỉ hiện tại
                if (address != null && addressId != currentAddressId) {
                    databaseRef.child(addressId).child("default").setValue(false)
                }
            }
        }
    }

    // Hàm thêm địa chỉ mới
    fun addAddress(address: Address) {
        if (address.default) {
            // Nếu địa chỉ mới là default, xóa default của các địa chỉ khác
            clearOtherDefaults(null)
        }
        val newAddressRef = databaseRef.push()
        val addressWithId = address.copy(id = newAddressRef.key ?: "")
        newAddressRef.setValue(addressWithId)
    }

    // Hàm cập nhật địa chỉ
    fun updateAddress(address: Address) {
        if (address.id.isNotEmpty()) {
            if (address.default) {
                // Nếu địa chỉ cập nhật là default, xóa default của các địa chỉ khác
                clearOtherDefaults(address.id)
            }
            val addressRef = databaseRef.child(address.id)
            addressRef.setValue(address)
        } else {
            Log.e("AddressViewModel", "Cannot update address with empty id")
        }
    }

    fun deleteAddress(addressId: String) {
        databaseRef.child(addressId).removeValue()
    }

    fun fetchAddressByPostcode(postcode: String) {
        zipcloudApiService.getAddressByZipcode(postcode).enqueue(object : Callback<ZipcloudResponse> {
            override fun onResponse(call: Call<ZipcloudResponse>, response: Response<ZipcloudResponse>) {
                if (response.isSuccessful && response.body()?.results != null) {
                    val result = response.body()?.results?.firstOrNull()
                    if (result != null) {
                        val autoFilled = "${result.address1} ${result.address2} ${result.address3}"
                        _autoFilledAddress.value = autoFilled
                        Log.d("AddressViewModel", "Auto-filled address: $autoFilled")
                    } else {
                        Log.e("AddressViewModel", "No results found for postcode.")
                    }
                } else {
                    Log.e("AddressViewModel", "Failed to fetch address: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ZipcloudResponse>, t: Throwable) {
                Log.e("AddressViewModel", "API call failed: ${t.message}")
            }
        })
    }
}