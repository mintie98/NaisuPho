package com.example.naisupho.model

data class Address(
    val id: String = "",
    val address1: String = "",  // Địa chỉ từ postcode
    val address2: String = "",  // Phần địa chỉ do người dùng thêm
    val postcode: String = "",
    val default: Boolean = false
)