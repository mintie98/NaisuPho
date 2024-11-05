package com.example.naisupho

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naisupho.adapter.CategoryAdapter
import com.example.naisupho.adapter.MenuAdapter
import com.example.naisupho.databinding.ActivitySearchBinding
import com.example.naisupho.model.Category
import com.example.naisupho.utils.StringUtils
import com.example.naisupho.viewmodel.MenuViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: MenuViewModel by viewModels()
    private var userLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userLocation = intent.getStringExtra("USER_LOCATION")
        Log.d("SearchActivity", "Received user location: $userLocation")
        // Fetch menu items từ Firebase
        viewModel.fetchMenuItems()
        viewModel.fetchStores()

        // Set up back button
        binding.backButton.setOnClickListener {
            finish()
        }

        setupCategoryRecyclerView()
        observeViewModel()
        setupSearchView()
    }

    private fun setupCategoryRecyclerView() {
        // Dummy data for categories
        val categories = listOf(
            Category("category_id1", "Noodle Soup", R.drawable.pho),
            Category("category_id2", "Rice dishes", R.drawable.pho),
            Category("category_id3", "Bánh Mì", R.drawable.pho),
//            Category("category_id4", "Soup&Congees", R.drawable.pho),
//            Category("category_id5", "Wraps&rolls", R.drawable.pho),
//            Category("category_id6", "Bánh", R.drawable.pho),
//            Category("category_id7", "Meat dishes", R.drawable.pho),
//            Category("category_id8", "Seafood dishes", R.drawable.pho),
//            Category("category_id9", "Salads", R.drawable.pho),
//            Category("category_id10", "Desserts", R.drawable.pho)
        )

        val adapter = CategoryAdapter(categories) { category ->
            // Xử lý sự kiện khi nhấn vào một category
            Log.d("SearchActivity", "Category clicked: ${category.name}")
            // Thực hiện lọc các sản phẩm theo category được chọn
            viewModel.filterMenuItemsByCategory(category.id)
        }

        // Sử dụng ViewBinding để truy cập RecyclerView
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.filteredMenuItems.observe(this) { filteredItems ->
            Log.d("SearchActivity", "Filtered Items: $filteredItems")
            val adapter = MenuAdapter(filteredItems, viewModel.stores.value ?: emptyMap(), this, userLocation) { userLocation, storeAddress, callback ->
                viewModel.fetchTravelTime(userLocation, storeAddress, callback)
            }
            binding.searchRecyclerView.layoutManager = GridLayoutManager(this, 2)
            binding.searchRecyclerView.adapter = adapter
        }
    }

    private fun setupSearchView() {
        // Đặt lắng nghe thay đổi của SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Xử lý khi người dùng nhấn nút "Tìm kiếm"
                query?.let {
                    performSearch(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Xử lý khi nội dung tìm kiếm thay đổi
                newText?.let {
                    if (it.isNotEmpty()) {
                        performSearch(it)
                    }
                }
                return true
            }
        })

        // Bỏ focus SearchView khi nhấn nút quay lại
        binding.searchView.setOnCloseListener {
            Log.d("SearchActivity", "SearchView closed")
            binding.searchView.clearFocus()
            true
        }
    }

    private fun performSearch(query: String) {
        val normalizedQuery = StringUtils.removeVietnameseAccents(query).lowercase()
        viewModel.searchMenuItems(normalizedQuery)
    }

}