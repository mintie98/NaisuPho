package com.example.naisupho.model

data class StoreCartItem(
    val storeId: String,
    val storeName: String,
    val storeImage: String,
    val itemQuantity: Int,
    val totalPrice: Int,
    val itemList: List<CartItems>
)
