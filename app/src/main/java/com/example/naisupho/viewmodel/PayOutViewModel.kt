package com.example.naisupho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.naisupho.model.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PayOutViewModel @Inject constructor() : ViewModel() {

    private val _addressList = MutableLiveData<List<Address>>()
    val addressList: LiveData<List<Address>> get() = _addressList

    private val _selectedAddress = MutableLiveData<Address?>()
    val selectedAddress: LiveData<Address?> get() = _selectedAddress

    private val _phoneNumber = MutableLiveData<String?>()
    val phoneNumber: LiveData<String?> get() = _phoneNumber

    private val _totalCost = MutableLiveData<Int>()
    val totalCost: LiveData<Int> get() = _totalCost

    private val _discount = MutableLiveData<Int>().apply { value = 0 }
    val discount: LiveData<Int> get() = _discount

    private val _deliveryFee = MutableLiveData<Int>().apply { value = 0 }
    val deliveryFee: LiveData<Int> get() = _deliveryFee

    private val _finalTotal = MutableLiveData<Int>()
    val finalTotal: LiveData<Int> get() = _finalTotal

    private val _userAuthorizationId = MutableLiveData<String?>()
    val userAuthorizationId: LiveData<String?> get() = _userAuthorizationId

    init {
        loadAddresses()
        fetchPhoneNumber()
        calculateFinalTotal()
        fetchUserAuthorizationId()
    }

    /**
     * Lấy danh sách địa chỉ từ Firebase Realtime Database
     */
    private fun loadAddresses() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$userId/addresses")
        databaseRef.get()
            .addOnSuccessListener { snapshot ->
                val addressList = snapshot.children.mapNotNull {
                    val address = it.getValue(Address::class.java)
                    sanitizeAddress(address) // Làm sạch và loại bỏ địa chỉ không hợp lệ
                }

                _addressList.value = addressList

                if (addressList.isNotEmpty()) {
                    // Chọn địa chỉ mặc định nếu có, ngược lại chọn địa chỉ đầu tiên
                    _selectedAddress.value = addressList.find { it.default == true } ?: addressList.first()
                } else {
                    _selectedAddress.value = null
                }

                Log.d("PayOutViewModel", "Address list: $addressList")
            }
            .addOnFailureListener {
                _addressList.value = emptyList()
                _selectedAddress.value = null
                Log.e("PayOutViewModel", "Failed to load addresses: ${it.message}")
            }
    }
    private fun sanitizeAddress(address: Address?): Address? {
        if (address == null) return null

        val postcode = address.postcode?.takeIf { it.isNotBlank() }
        val address1 = address.address1?.takeIf { it.isNotBlank() }
        val address2 = address.address2?.takeIf { it.isNotBlank() }

        return if (postcode == null && address1 == null && address2 == null) {
            null
        } else {
            address.copy(
                postcode = postcode ?: "",
                address1 = address1 ?: "",
                address2 = address2 ?: ""
            )
        }
    }

    /**
     * Cập nhật địa chỉ giao hàng đã chọn
     */
    fun updateSelectedAddress(address: Address) {
        _selectedAddress.value = sanitizeAddress(address) ?: Address("", "", "")
    }

    /**
     * Lấy số điện thoại từ Firebase Realtime Database
     */
    private fun fetchPhoneNumber() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val phoneRef = FirebaseDatabase.getInstance().getReference("Users/$userId/phoneNumber")
        phoneRef.get()
            .addOnSuccessListener { snapshot ->
                _phoneNumber.value = snapshot.getValue(String::class.java) ?: "No phone number"
            }
            .addOnFailureListener {
                _phoneNumber.value = null
                Log.e("PayOutViewModel", "Failed to fetch phone number: ${it.message}")
            }
    }

    /**
     * Cập nhật số điện thoại lên Firebase Realtime Database
     */
    fun updatePhone(phoneNumber: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("PayOutViewModel", "Updating phone number: $phoneNumber")
        Log.d("PayOutViewModel", "Current user ID: $userId")

        val phoneRef = FirebaseDatabase.getInstance().getReference("Users/$userId/phoneNumber")
        phoneRef.setValue(phoneNumber) // Trực tiếp dùng phoneNumber đã xác minh
            .addOnSuccessListener {
                _phoneNumber.value = phoneNumber
                Log.d("PayOutViewModel", "Phone number updated successfully: $phoneNumber")
            }
            .addOnFailureListener {
                Log.e("PayOutViewModel", "Failed to update phone number: ${it.message}")
            }
    }

    /**
     * Cập nhật tổng chi phí
     */
    fun setTotalCost(total: Int) {
        _totalCost.value = total
        calculateFinalTotal()
    }

    fun setDiscount(amount: Int) {
        _discount.value = amount
        calculateFinalTotal()
    }

    fun setDeliveryFee(amount: Int) {
        _deliveryFee.value = amount
        calculateFinalTotal()
    }

    private fun calculateFinalTotal() {
        val total = (_totalCost.value ?: 0) - (_discount.value ?: 0) + (_deliveryFee.value ?: 0)
        _finalTotal.value = total
    }

    /**
     * Lấy thông tin xác thực PayPay từ Firebase Realtime Database
     */
    private fun fetchUserAuthorizationId() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val databaseRef = FirebaseDatabase.getInstance().getReference("paymentMethods/$userId/methods")
        databaseRef.get()
            .addOnSuccessListener { snapshot ->
                val userAuthId = snapshot.children
                    .mapNotNull { it.child("userAuthorizationId").value as? String }
                    .firstOrNull()
                _userAuthorizationId.value = userAuthId
                Log.d("PayOutViewModel", "Fetched userAuthorizationId: $userAuthId")
            }
            .addOnFailureListener { error ->
                Log.e("PayOutViewModel", "Failed to fetch userAuthorizationId: ${error.message}")
                _userAuthorizationId.value = null
            }
    }

    fun addAddress(newAddress: Address) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$userId/addresses")
        val newAddressRef = databaseRef.push()
        val addressWithId = newAddress.copy(id = newAddressRef.key ?: "")
        newAddressRef.setValue(addressWithId).addOnSuccessListener {
            loadAddresses() // Tải lại danh sách địa chỉ
        }
    }
    fun fetchAddresses() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$userId/addresses")

        databaseRef.get().addOnSuccessListener { snapshot ->
            val addressList = snapshot.children.mapNotNull { it.getValue(Address::class.java) }
            _addressList.value = addressList

            // Log để kiểm tra danh sách
            Log.d("PayOutViewModel", "Fetched addresses: $addressList")
        }.addOnFailureListener { error ->
            _addressList.value = emptyList()
            Log.e("PayOutViewModel", "Failed to fetch addresses: ${error.message}")
        }
    }

}