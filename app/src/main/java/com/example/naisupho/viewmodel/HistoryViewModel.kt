package com.example.naisupho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.naisupho.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _transactionList = MutableLiveData<List<Transaction>>()
    val transactionList: LiveData<List<Transaction>> get() = _transactionList

    fun loadTransactions() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val transactionsRef = firebaseDatabase.getReference("transactions/$userId")

        transactionsRef.get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.children.mapNotNull {
                    it.getValue(Transaction::class.java)
                }
                _transactionList.value = transactions
            }
            .addOnFailureListener {
                _transactionList.value = emptyList() // handle error
            }
    }
}