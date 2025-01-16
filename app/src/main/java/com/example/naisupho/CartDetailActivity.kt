package com.example.naisupho

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.adapter.CartItemAdapter
import com.example.naisupho.databinding.ActivityCartDetailBinding
import com.example.naisupho.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityCartDetailBinding
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var cartItemAdapter: CartItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storeId = intent.getStringExtra("StoreId") ?: return
        cartViewModel.isCartEmpty.observe(this) { isCartEmpty ->
            if (isCartEmpty) {
                finish() // Đóng Activity khi giỏ hàng bị xóa
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
        setupRecyclerView(storeId)
        binding.checkoutButton.setOnClickListener {
            val intent = Intent(this, PayOutActivity::class.java)
            intent.putExtra("totalCost", cartViewModel.totalCost.value ?: 0)
            intent.putExtra("storeId", storeId)
            intent.putExtra("storeName", binding.storeName.text)
            startActivity(intent)
            finish()
        }
        binding.orderMoreButton.setOnClickListener {
            val intent = Intent(this, StoreActivity::class.java)
            intent.putExtra("StoreId", storeId)
            startActivity(intent)
        }

        cartViewModel.fetchStoreName(storeId) { storeName ->
            binding.storeName.text = storeName
        }

        cartViewModel.fetchCartItemsForStore(storeId)
        observeCartItems()
        observeTotalCost()
    }

    private fun setupRecyclerView(storeId: String) {
        cartItemAdapter = CartItemAdapter(this, storeId, cartViewModel,false) {
            cartViewModel.deleteCart(storeId) // Xóa CartStoreItem của cửa hàng đó
            finish() // Kết thúc CartDetailActivity
        }
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.cartRecyclerView.adapter = cartItemAdapter
    }

    private fun observeCartItems() {
        cartViewModel.cartItems.observe(this) { cartItems ->
            cartItemAdapter.updateItems(cartItems) // Cập nhật qua phương thức updateItems
        }
    }
    private fun observeTotalCost() {
        cartViewModel.totalCost.observe(this) { total ->
            binding.totalCost.text = "￥$total"
        }
    }
}