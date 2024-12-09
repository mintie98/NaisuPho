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
import com.example.naisupho.di.MyApp
import com.example.naisupho.viewmodel.HomeViewModel
import com.example.naisupho.viewmodel.StoreViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreActivity : BaseActivity() {
    private lateinit var binding: ActivityStoreBinding
    private val storeViewModel: StoreViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var storeMenuAdapter: StoreMenuAdapter
    private var storeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nhận storeId từ Intent
        storeId = intent.getStringExtra("StoreId")

        // Sử dụng HomeViewModel để lấy thông tin cửa hàng khi có userLocation
        (application as MyApp).userLocation?.let { userLocation ->
            storeId?.let { id ->
                homeViewModel.fetchStores(userLocation) // Lấy thông tin cửa hàng từ HomeViewModel
                observeStoreDetails(id) // Quan sát thông tin cửa hàng và thời gian di chuyển
            }
        }

        setupUI()
        setupRecyclerView()
        observeCategoryViewModel()

        // Lấy các danh mục và món từ StoreViewModel
        storeId?.let { storeViewModel.fetchCategories(it) }
    }

    private fun setupUI() {
        binding.backArrowImageView.setOnClickListener { finish() }
        setupTabLayout()
    }

    private fun setupRecyclerView() {
        storeMenuAdapter = StoreMenuAdapter(emptyList(), this)
        binding.menuRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.menuRecyclerView.adapter = storeMenuAdapter
    }

    private fun setupTabLayout() {
        val allMenuTab = binding.tabLayout.newTab().setText(getString(R.string.all_menu))
        binding.tabLayout.addTab(allMenuTab)
        allMenuTab.tag = getString(R.string.all_menu)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isAllMenuTab = tab?.tag == getString(R.string.all_menu)
                if (isAllMenuTab) {
                    storeViewModel.getAllItems()
                } else {
                    val categoryId = tab?.tag as? String
                    categoryId?.let { storeViewModel.filterItemsByCategory(it) }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeStoreDetails(storeId: String) {
        // Quan sát danh sách cửa hàng để lấy thông tin chi tiết của cửa hàng hiện tại
        homeViewModel.stores.observe(this, Observer { storesList ->
            storesList.find { it.storeId == storeId }?.let { store ->
                binding.storeNameTextView.text = getString(R.string.store_name_format, store.storeName)
                binding.storeAddressTextView.text = getString(R.string.store_address_format, store.storeAddress)
                Glide.with(this).load(Uri.parse(store.storePhotoUrl)).into(binding.storeImageView)
                binding.storeRatingBar.rating = store.storeRate ?: 0.0f
            }
        })

        // Quan sát thời gian di chuyển cho cửa hàng hiện tại
        homeViewModel.travelTimes.observe(this, Observer { travelTimes ->
            val travelTime = travelTimes[storeId] ?: "N/A"
            binding.travelTimeTextView.text = getString(R.string.estimated_delivery_time, travelTime)
        })
    }

    private fun observeCategoryViewModel() {
        storeViewModel.menuItems.observe(this, Observer { menuItems ->
            storeMenuAdapter.updateItems(menuItems)
        })

        storeViewModel.categories.observe(this, Observer { categories ->
            categories.forEach { (categoryId, categoryName) ->
                val tab = binding.tabLayout.newTab().setText(categoryName)
                tab.tag = categoryId
                binding.tabLayout.addTab(tab)
            }
        })
    }
}