package com.example.naisupho.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.CartDetailActivity
import com.example.naisupho.R
import com.example.naisupho.databinding.CartItemBinding
import com.example.naisupho.model.CartItems
import com.example.naisupho.viewmodel.CartViewModel

// CartItemAdapter.kt
class CartItemAdapter(
    private val context: Context,
    private val storeId: String,
    private val cartViewModel: CartViewModel,
    private val onLastItemDeleted: () -> Unit
) : RecyclerView.Adapter<CartItemAdapter.CartViewHolder>() {

    private var cartItems: List<CartItems> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position], position)
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newCartItems: List<CartItems>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }

    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItems, position: Int) {
            binding.apply {
                val totalItemPrice = (cartItem.itemPrice ?: 0) * (cartItem.itemQuantity ?: 1)
                cartItemName.text = cartItem.itemName
                cartItemPrice.text = "¥${totalItemPrice}"
                cartItemQuantity.text = cartItem.itemQuantity.toString()

                Glide.with(context)
                    .load(Uri.parse(cartItem.itemImage))
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.pho)
                    .into(cartImage)

                minusbutton.setOnClickListener {
                    val oldQuantity = cartItem.itemQuantity ?: 1
                    val newQuantity = oldQuantity - 1
                    if (newQuantity > 0) {
                        val itemKey = cartViewModel.getItemKeyAt(position)
                        if (itemKey.isNotEmpty()) {
                            cartViewModel.updateItemQuantity(storeId, itemKey, newQuantity)
                            val priceDifference = (cartItem.itemPrice ?: 0) * (newQuantity - oldQuantity)
                            cartViewModel.updateTotalCost(priceDifference) // Cập nhật tổng giá với phần chênh lệch
                        }
                    }
                }

                plusebutton.setOnClickListener {
                    val oldQuantity = cartItem.itemQuantity ?: 1
                    val newQuantity = oldQuantity + 1
                    if (newQuantity <= 20) {
                        val itemKey = cartViewModel.getItemKeyAt(position)
                        if (itemKey.isNotEmpty()) {
                            cartViewModel.updateItemQuantity(storeId, itemKey, newQuantity)
                            val priceDifference = (cartItem.itemPrice ?: 0) * (newQuantity - oldQuantity)
                            cartViewModel.updateTotalCost(priceDifference) // Cập nhật tổng giá với phần chênh lệch
                        }
                    }
                }

                deleteButton.setOnClickListener {
                    showDeleteConfirmationDialog(cartItem,position)
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(cartItem: CartItems, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                // Xóa mục khỏi cart
                cartViewModel.deleteCartItem(storeId, position)
                if (cartItems.size == 1) { // Nếu đây là mục cuối cùng
                    onLastItemDeleted() // Gọi callback để thông báo CartDetailActivity kết thúc
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}