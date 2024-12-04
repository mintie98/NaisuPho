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
        calculateFinalTotal()
        fetchUserAuthorizationId()
    }

    private fun loadAddresses() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _addressList.value = emptyList()
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$userId/addresses")
        databaseRef.get()
            .addOnSuccessListener { snapshot ->
                val addressList = snapshot.children.mapNotNull {
                    val address = it.getValue(Address::class.java)
                    if (address == null) {
                        Log.e("PayOutViewModel", "Invalid address format: ${it.value}")
                    }
                    address
                }
                _addressList.value = addressList

                // Log để kiểm tra danh sách
                Log.d("PayOutViewModel", "Address list: $addressList")

                // Chọn địa chỉ mặc định
                val defaultAddress = addressList.find { it.default == true }
                _selectedAddress.value = defaultAddress
            }
            .addOnFailureListener {
                _addressList.value = emptyList()
                Log.e("PayOutViewModel", "Failed to load addresses: ${it.message}")
            }
    }

    fun updateSelectedAddress(address: Address) {
        _selectedAddress.value = address
    }

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
    private fun fetchUserAuthorizationId() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("PayOutViewModel", "User not logged in")
            _userAuthorizationId.value = null
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("paymentMethods/$userId/methods")
        databaseRef.get()
            .addOnSuccessListener { snapshot ->
                val userAuthId = snapshot.children
                    .mapNotNull { it.child("userAuthorizationId").value as? String }
                    .firstOrNull() // Lấy userAuthorizationId đầu tiên (hoặc mặc định)
                _userAuthorizationId.value = userAuthId
                Log.d("PayOutViewModel", "Fetched userAuthorizationId: $userAuthId")
            }
            .addOnFailureListener { error ->
                Log.e("PayOutViewModel", "Failed to fetch userAuthorizationId: ${error.message}")
                _userAuthorizationId.value = null
            }
    }
}