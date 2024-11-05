package com.example.naisupho

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.naisupho.adapter.StoreMenuAdapter
import com.example.naisupho.databinding.ActivityStoreBinding
import com.example.naisupho.viewmodel.CategoryViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoreBinding
    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var storeMenuAdapter: StoreMenuAdapter
    private var storeId: String? = null
    private var storeName: String? = null
    private var storeAddress: String? = null
    private var storePhotoUrl: String? = null
    private var moveTime: String? = null
    private var rate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storeId = intent.getStringExtra("StoreId")
        storeName = intent.getStringExtra("StoreName")
        storeAddress = intent.getStringExtra("StoreAddress")
        storePhotoUrl = intent.getStringExtra("StorePhotoUrl")
        moveTime = intent.getStringExtra("MoveTime")
        rate = intent.getStringExtra("Rate")

        binding.storeNameTextView.text = "Store Name :${storeName}"
        binding.storeAddressTextView.text = "Store Address:${storeAddress}"
        binding.travelTimeTextView.text = "Estimated Delivery Time: ~${moveTime}"
        val uri = Uri.parse(storePhotoUrl)
        Glide.with(this).load(uri).into(binding.storeImageView)
        rate?.toFloatOrNull()?.let { rating ->
            binding.storeRatingBar.rating = rating // Gán giá trị rate cho RatingBar
        } ?: run {
            binding.storeRatingBar.rating = 0.0f // Giá trị mặc định nếu rate là null hoặc không hợp lệ
        }


        binding.backArrowImageView.setOnClickListener {
            finish()
        }

        setupTabLayout()
        setupRecyclerView()
        observeViewModel()

        categoryViewModel.fetchCategories(storeId)
    }

    private fun setupRecyclerView() {
        storeMenuAdapter = StoreMenuAdapter(emptyList(), this)
        binding.menuRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.menuRecyclerView.adapter = storeMenuAdapter
    }

    private fun setupTabLayout() {
        val allMenuTab = binding.tabLayout.newTab().setText("All Menu")
        binding.tabLayout.addTab(allMenuTab)
        allMenuTab.tag = "All Menu" // Đặt tag để xác định đây là tab hiển thị tất cả

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isAllMenuTab = tab?.tag == "All Menu"
                if (isAllMenuTab) {
                    // Gọi getAllItems() để hiển thị tất cả item khi chọn tab "All Menu"
                    categoryViewModel.getAllItems()
                } else {
                    val categoryId = tab?.tag as? String
                    if (categoryId != null) {
                        categoryViewModel.filterItemsByCategory(categoryId)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        categoryViewModel.menuItems.observe(this, Observer { menuItems ->
            storeMenuAdapter.updateItems(menuItems)
        })

        categoryViewModel.categories.observe(this, Observer { categories ->
            categories.forEach { (categoryId, categoryName) ->
                val tab = binding.tabLayout.newTab().setText(categoryName)
                tab.tag = categoryId // Đặt categoryId vào tag để sử dụng khi lọc
                binding.tabLayout.addTab(tab)
            }

            storeMenuAdapter.updateItems(categoryViewModel.menuItems.value ?: emptyList())
        })
    }
}