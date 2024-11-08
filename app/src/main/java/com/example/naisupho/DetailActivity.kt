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
    private var itemPrice : Int? = null
    private var itemImage : String? = null
    private var itemDetail : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        itemName = intent.getStringExtra("MenuItemName")
        itemPrice = intent.getIntExtra("MenuItemPrice", 0)
        itemImage = intent.getStringExtra("MenuItemImage")
        itemDetail = intent.getStringExtra("MenuItemDetail")

        binding.detailItemName.text = itemName
        binding.detailItemPrice.text = "Price: ￥$itemPrice"
        binding.detailItemDescription.text = itemDetail
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

        // Tạo đối tượng CartItems với thông tin cần lưu
        val cartItem = CartItems(
            itemName = itemName,
            itemPrice = itemPrice,
            itemImage = itemImage,
            itemQuantity = quantity
        )

        // Lưu vào nhánh CartItems của người dùng
        val cartRef = database.child("CartItems").child(userId).push()

        cartRef.setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Cart Item saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "Failed to save Cart Item", Toast.LENGTH_SHORT).show()
            }
    }
}