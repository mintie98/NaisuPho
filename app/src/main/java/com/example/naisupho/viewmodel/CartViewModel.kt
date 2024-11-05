package com.example.naisupho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.naisupho.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItems>>()
    val cartItems: LiveData<List<CartItems>> get() = _cartItems

    private val _cartItemKeys = MutableLiveData<List<String>>() // Thêm LiveData để lưu trữ khóa của các item
    val cartItemKeys: LiveData<List<String>> get() = _cartItemKeys

    private val _currentDiscountCode = MutableLiveData<String>()
    val currentDiscountCode: LiveData<String> get() = _currentDiscountCode

    private val _originalPrice = MutableLiveData<Int>()
    val originalPrice: LiveData<Int> get() = _originalPrice

    private val _discountPrice = MutableLiveData<Int>()
    val discountPrice: LiveData<Int> get() = _discountPrice

    private val _totalPrice = MutableLiveData<Int>()
    val totalPrice: LiveData<Int> get() = _totalPrice

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid ?: ""
    private val cartRef = FirebaseDatabase.getInstance().reference.child("CartItems").child(userId)

    init {
        fetchCartItems()
    }

    fun fetchCartItems() {
        cartRef.get().addOnSuccessListener { snapshot ->
            val cartItemsList = mutableListOf<CartItems>()
            val cartItemKeysList = mutableListOf<String>()

            snapshot.children.forEach { itemSnapshot ->
                val cartItem = itemSnapshot.getValue(CartItems::class.java)
                if (cartItem != null) {
                    cartItemsList.add(cartItem)
                    cartItemKeysList.add(itemSnapshot.key ?: "")
                }
            }

            _cartItems.value = cartItemsList
            _cartItemKeys.value = cartItemKeysList // Cập nhật danh sách khóa Firebase
            calculateTotalPrice(cartItemsList)
        }.addOnFailureListener {
            Log.e("CartViewModel", "Failed to load cart items: ${it.message}")
        }
    }

    fun setDiscountCode(discountCode: String) {
        _currentDiscountCode.value = discountCode
        calculateTotalPrice(_cartItems.value ?: emptyList())
    }

    private fun calculateTotalPrice(cartItems: List<CartItems>) {
        val originalTotal = cartItems.sumBy { (it.itemPrice ?: 0) * (it.itemQuantity ?: 0) }

        // Áp dụng mã giảm giá nếu có
        val discountPercent = when (_currentDiscountCode.value) {
            "GIAM10" -> 0.10
            "GIAM20" -> 0.20
            else -> 0.0
        }

        // Tính giá trị giảm giá và tổng tiền cuối cùng
        val discountValue = (originalTotal * discountPercent).toInt()
        val finalTotal = originalTotal - discountValue

        _originalPrice.value = originalTotal
        _discountPrice.value = discountValue
        _totalPrice.value = finalTotal
    }
}