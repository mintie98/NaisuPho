package com.example.naisupho.model

data class Transaction(
    val transactionId: String = "",
    val storeName: String = "",
    val timestamp: Long = 0,
    val amount: Int = 0,
    val status: String = "",
    val items: List<TransactionItem> = emptyList()
)

data class TransactionItem(
    val name: String = "",
    val quantity: Int = 0,
    val price: Int = 0,
    val image: String = ""
)
