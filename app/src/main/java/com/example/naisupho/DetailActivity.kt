package com.example.naisupho

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.naisupho.databinding.ActivityDetailBinding
import com.example.naisupho.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailBinding
    private lateinit var mAuth: FirebaseAuth
    private var itemName : String? = null
    private var itemPrice : String? = null
    private var itemImage : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        itemName = intent.getStringExtra("MenuItemName")
        itemPrice = intent.getStringExtra("MenuItemPrice")
        itemImage = intent.getStringExtra("MenuItemImage")
        binding.detailItemName.text = itemName
        binding.detailItemPrice.text = "Price: ￥$itemPrice"
        val uri = Uri.parse(itemImage)
        val itemImageView = binding.DetailItemImage
        Glide.with(this).load(uri).into(itemImageView)
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.decrementBtn.setOnClickListener {
            val quantity = binding.quantityText.text.toString().toInt()
            if (quantity > 1) {
                binding.quantityText.text = (quantity - 1).toString()
            }
        }
        binding.incrementBtn.setOnClickListener {
            val quantity = binding.quantityText.text.toString().toInt()
            binding.quantityText.text = (quantity + 1).toString()
        }
        binding.addToCartBtn.setOnClickListener {
            addItemToCart()
        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = mAuth.currentUser?.uid ?: ""
        val quantity = binding.quantityText.text.toString().toInt()

        val cartItem = CartItems(itemName.toString(), itemPrice.toString(), itemImage.toString(), itemQuantity = quantity)
        database.child("Users").child(userId).child("CartItems").push().setValue(cartItem)
            .addOnSuccessListener {
                finish()
                Toast.makeText(applicationContext, "Cart Item saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Failed to save Cart Item", Toast.LENGTH_SHORT).show()
            }
    }
}