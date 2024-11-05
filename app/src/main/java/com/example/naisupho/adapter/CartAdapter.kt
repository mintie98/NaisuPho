package com.example.naisupho.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.databinding.CartItemBinding
import com.example.naisupho.interfaces.CartInteractionListener
import com.example.naisupho.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CartAdapter(
    private val context: Context,
    private var cartItems: MutableList<CartItems>,
    private var cartItemKeys: MutableList<String>, // Thêm danh sách các khóa Firebase
    private val listener: CartInteractionListener
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    init {
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        cartItemsRef = database.reference.child("CartItems").child(userId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val cartItem = cartItems[position]
            binding.apply {
                val totalItemPrice = (cartItem.itemPrice ?: 0) * (cartItem.itemQuantity ?: 1)
                cartItemName.text = cartItem.itemName
                cartItemPrice.text = "¥${totalItemPrice}"
                cartItemQuantity.text = cartItem.itemQuantity.toString()

                Glide.with(context).load(Uri.parse(cartItem.itemImage)).into(cartImage)

                minusbutton.setOnClickListener {
                    updateQuantity(position, cartItem.itemQuantity?.minus(1) ?: 1)
                }

                plusebutton.setOnClickListener {
                    updateQuantity(position, cartItem.itemQuantity?.plus(1) ?: 1)
                }

                deleteButton.setOnClickListener {
                    deleteItem(position)
                }
            }
        }

        private fun updateQuantity(position: Int, newQuantity: Int) {
            if (newQuantity > 0 && newQuantity <= 20) {
                cartItems[position].itemQuantity = newQuantity
                cartItemsRef.child(getUniqueKeyAtPosition(position)).child("itemQuantity")
                    .setValue(newQuantity)
                notifyItemChanged(position)
                listener.onCartQuantityChanged()
            }
        }

        private fun deleteItem(position: Int) {
            val itemKey = getUniqueKeyAtPosition(position)
            cartItemsRef.child(itemKey).removeValue().addOnSuccessListener {
                cartItems.removeAt(position)
                cartItemKeys.removeAt(position) // Cập nhật danh sách khóa
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartItems.size)
                Toast.makeText(context, "Item removed successfully", Toast.LENGTH_SHORT).show()
                listener.onCartQuantityChanged() // Cập nhật số lượng item trên icon
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private lateinit var cartItemsRef: DatabaseReference
    }

    private fun getUniqueKeyAtPosition(position: Int): String {
        // Trả về khóa Firebase dựa trên vị trí
        return cartItemKeys[position]
    }
}