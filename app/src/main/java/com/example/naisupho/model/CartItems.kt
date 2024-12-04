package com.example.naisupho.model

data class CartItems(
    var storeId: String? = null,
    var itemName: String? = null,
    var itemPrice: Int? = null,
    var itemImage: String? = null,
    var itemQuantity: Int? = null,
    val itemKey: String? = null
)
