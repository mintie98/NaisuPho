package com.example.naisupho.model

data class CartItems(
    var itemName: String? = null,
    var itemPrice: String? = null,
    var itemImage: String? = null,
    var itemQuantity: Int? = null
) {
    // Secondary constructor with parameters
    constructor(itemName: String, itemPrice: String, itemImage: String, itemQuantity: Int) : this() {
        this.itemName = itemName
        this.itemPrice = itemPrice
        this.itemImage = itemImage
        this.itemQuantity = itemQuantity
    }
}
