package com.example.naisupho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naisupho.model.Coupon
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _coupons = MutableLiveData<List<Coupon>>()
    val coupons: LiveData<List<Coupon>> = _coupons

    fun refreshCoupons() {
        viewModelScope.launch(Dispatchers.IO) {
            val couponsRef = database.getReference("Coupons")
            couponsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val couponList = mutableListOf<Coupon>()
                    for (couponSnapshot in snapshot.children) {
                        val coupon = couponSnapshot.getValue(Coupon::class.java)
                        coupon?.let {
                            it.id = couponSnapshot.key ?: ""
                            couponList.add(it)
                        }
                    }
                    _coupons.postValue(couponList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Xử lý lỗi ở đây, ví dụ: log lỗi hoặc cập nhật UI
                }
            })
        }
    }
}