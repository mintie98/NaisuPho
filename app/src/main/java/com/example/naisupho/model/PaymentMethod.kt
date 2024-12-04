package com.example.naisupho.model

data class PaymentMethod(
    val icon: String = "",
    val isSelected: Boolean = false,
    val provider: String = "",
    val type: String = "",
    val walletId: String = ""
)