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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailActivity : BaseActivity() {

    private lateinit var binding: ActivityDetailBinding

    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var database: DatabaseReference

    private var itemName: String? = null
    private var itemPrice: Int? = null
    private var itemImage: String? = null
    private var itemDetail: String? = null
    private var storeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemName = intent.getStringExtra("MenuItemName")
        itemPrice = intent.getIntExtra("MenuItemPrice", 0)
        itemImage = intent.getStringExtra("MenuItemImage")
        itemDetail = intent.getStringExtra("MenuItemDetail")
        storeId = intent.getStringExtra("StoreId")

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.detailItemName.text = itemName
        binding.detailItemPrice.text = "Price: ￥$itemPrice"
        binding.detailItemDescription.text = itemDetail

        val uri = Uri.parse(itemImage)
        Glide.with(this).load(uri).into(binding.DetailItemImage)
    }

    private fun setupListeners() {
        binding.backBtn.setOnClickListener { finish() }

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

        binding.addToCartBtn.setOnClickListener { addItemToCart() }
    }

    private fun addItemToCart() {
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
        val cartRef = database.child("CartItems").child(userId).child(storeId.toString()).push()

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