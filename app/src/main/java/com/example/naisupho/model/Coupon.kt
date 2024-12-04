package com.example.naisupho.model

data class Coupon(
    var id: String = "",
    val description: String = "",
    val discount_percent: Int = 0,
    val is_active: Boolean = false,
    val usage_limit: Int = 0
)
