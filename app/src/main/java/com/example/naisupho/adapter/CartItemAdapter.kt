package com.example.naisupho.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.R
import com.example.naisupho.databinding.CartItemBinding
import com.example.naisupho.databinding.CartItemSmallBinding
import com.example.naisupho.model.CartItems
import com.example.naisupho.viewmodel.CartViewModel

// CartItemAdapter.kt
class CartItemAdapter(
    private val context: Context,
    private val storeId: String,
    private val cartViewModel: CartViewModel,
    private val isCompactMode: Boolean = false, // Thêm chế độ nhỏ gọn
    private val onLastItemDeleted: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var cartItems: List<CartItems> = emptyList()

    companion object {
        private const val VIEW_TYPE_COMPACT = 0
        private const val VIEW_TYPE_FULL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isCompactMode) VIEW_TYPE_COMPACT else VIEW_TYPE_FULL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_COMPACT) {
            val binding = CartItemSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CompactViewHolder(binding)
        } else {
            val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            FullViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cartItem = cartItems[position]
        when (holder) {
            is CompactViewHolder -> holder.bind(cartItem)
            is FullViewHolder -> holder.bind(cartItem, position)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateItems(newCartItems: List<CartItems>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }

    // ViewHolder cho chế độ nhỏ gọn
    inner class CompactViewHolder(private val binding: CartItemSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItems) {
            binding.apply {
                cartItemName.text = cartItem.itemName
                cartItemPrice.text = "¥${(cartItem.itemPrice ?: 0) * (cartItem.itemQuantity ?: 1)}"
                cartItemQuantity.text = "x${cartItem.itemQuantity ?: 1}"

                Glide.with(context)
                    .load(Uri.parse(cartItem.itemImage))
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.pho)
                    .into(cartItemImage)
            }
        }
    }

    // ViewHolder cho chế độ đầy đủ
    inner class FullViewHolder(private val binding: CartItemBinding) :
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
                            cartViewModel.updateTotalCost(priceDifference)
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
                            cartViewModel.updateTotalCost(priceDifference)
                        }
                    }
                }

                deleteButton.setOnClickListener {
                    showDeleteConfirmationDialog(cartItem, position)
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(cartItem: CartItems, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { _, _ ->
                cartViewModel.deleteCartItem(storeId, position)
                if (cartItems.size == 1) {
                    onLastItemDeleted()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}