package com.example.naisupho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.naisupho.model.PaymentMethod
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: DatabaseReference
) : ViewModel() {

    private val _paymentMethods = MutableLiveData<List<PaymentMethod>>()
    val paymentMethods: LiveData<List<PaymentMethod>> get() = _paymentMethods

    fun fetchPaymentMethods() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _paymentMethods.value = emptyList()
            return
        }

        val userId = currentUser.uid
        val paymentMethodsRef = database.child("paymentMethods/$userId/methods")

        paymentMethodsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val methods = snapshot.children.mapNotNull {
                    it.getValue(PaymentMethod::class.java)
                }
                _paymentMethods.value = methods
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PaymentViewModel", "Error fetching payment methods: ${error.message}")
            }
        })
    }

    fun updateSelectedPaymentMethod(selectedMethod: PaymentMethod) {
        val currentMethods = _paymentMethods.value?.map { method ->
            method.copy(isSelected = method == selectedMethod)
        } ?: return
        _paymentMethods.value = currentMethods
    }
}