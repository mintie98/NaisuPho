package com.example.naisupho.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.naisupho.databinding.CartItemBinding
import com.example.naisupho.model.CartInteractionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartAdapter(private  val context : Context, val cartItems: MutableList<String>, private val CartItemPrice: MutableList<String>, private var CartImage: MutableList<String>, private var CartQuantity: MutableList<Int>,private val listener: CartInteractionListener) : RecyclerView.Adapter<CartAdapter.CartViewHolder>(){
    private val auth = FirebaseAuth.getInstance()


    init {
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val carditemnumber = cartItems.size
        itemQuantities = IntArray(carditemnumber) { CartQuantity[it] }
        cartItemsRef = database.reference.child("Users").child(userId).child("CartItems")

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
            binding.apply {
                val quantity = itemQuantities[position]
                cartItemName.text = cartItems[position]
                cartItemPrice.text = "¥${CartItemPrice[position]}"
                val uriString = CartImage[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(cartImage)
//                cartImage.setImageResource(CartImage[position])
                cartItemQuantity.text = quantity.toString()
                minusbutton.setOnClickListener {
                    deceaseQuantity(position)
                }
                plusebutton.setOnClickListener {
                    increaseQuantity(position)
                }
                deleteButton.setOnClickListener {
                    val itemPosition = adapterPosition

                    if (itemPosition != RecyclerView.NO_POSITION) {
                        deleteItem(itemPosition)
                    }
                }
            }
        }

        private fun increaseQuantity(position: Int) {
            if (CartQuantity[position] < 10) {
                CartQuantity[position]++
                binding.cartItemQuantity.text = CartQuantity[position].toString()
                updateQuantityInFirebase(position, CartQuantity[position])
                listener.onCartQuantityChanged()
            }
        }

        private fun deceaseQuantity(position: Int) {
            if (CartQuantity[position] > 1) {
                CartQuantity[position]--
                binding.cartItemQuantity.text = CartQuantity[position].toString()
                updateQuantityInFirebase(position, CartQuantity[position])
                listener.onCartQuantityChanged()
            }
        }

        @SuppressLint("SuspiciousIndentation")
        private fun deleteItem(position: Int) {

            val positionToRetrieve = position // Replace with the positions you want to retrieve

            getUniqueKeyAtPosition(positionToRetrieve) { uniqueKey ->

                if (uniqueKey != null) {
                    removeItem(position,uniqueKey)
                }


            }
        }
        private fun updateQuantityInFirebase(position: Int, quantity: Int) {
            getUniqueKeyAtPosition(position) { uniqueKey ->
                if (uniqueKey != null) {
                    cartItemsRef.child(uniqueKey).child("itemQuantity").setValue(quantity)
                }
            }
        }
    }

    companion object {
        private var itemQuantities: IntArray =
            intArrayOf() // here itemquantities is companion object and initialising in init block
        private lateinit var cartItemsRef: DatabaseReference
        // beacause it cannot access carditems member that's why (line no 14 this file)
    }

    fun getUniqueKeyAtPosition(positiontoreterive: Int, onComplete: (String?) -> Unit) {
        cartItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var uniqueKey: String? = null

                // Loop through the snapshot children
                snapshot.children.forEachIndexed { index, childSnapshot ->
                    if (index == positiontoreterive) {
                        uniqueKey = childSnapshot.key
                        return@forEachIndexed
                    }
                }

                onComplete(uniqueKey)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                onComplete(null)
            }
        })


    }
    private fun  removeItem(position: Int, itemKey: String)
    {
        if (itemKey!=null) {
            cartItemsRef.child(itemKey).removeValue().addOnSuccessListener {

                Toast.makeText(context,"remove", Toast.LENGTH_LONG).show()
                cartItems.removeAt(position)
                CartImage.removeAt(position)
                CartItemPrice.removeAt(position)
                CartQuantity.removeAt(position)
                itemQuantities =
                    itemQuantities.filterIndexed { index, _ -> index != position }.toIntArray()

                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartItems.size)

            }.addOnFailureListener {
                 // Handle failure if needed
                Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()

            }
        }
    }

    public fun getUpdatedQuantity() : MutableList<Int> {
        CartQuantity.clear()
        CartQuantity.addAll(itemQuantities.toList())
        return CartQuantity
    }
}