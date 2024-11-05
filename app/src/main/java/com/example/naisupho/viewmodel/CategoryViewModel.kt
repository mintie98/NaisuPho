package com.example.naisupho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.naisupho.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor() : ViewModel() {
    private val _categories = MutableLiveData<List<Pair<String, String>>>() // List of Pair<categoryId, categoryName>
    val categories: LiveData<List<Pair<String, String>>> get() = _categories

    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> get() = _menuItems

    private val allItemsList = mutableListOf<MenuItem>()

    fun fetchCategories(storeId: String?) {
        if (storeId == null) return

        val categoryRef = FirebaseDatabase.getInstance().reference.child("menuItem")

        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val categoryList = mutableListOf<Pair<String, String>>()
                allItemsList.clear()

                for (categorySnapshot in dataSnapshot.children) {
                    val categoryId = categorySnapshot.key ?: continue
                    val categoryName = categorySnapshot.child("category_name").getValue(String::class.java)
                    val itemsSnapshot = categorySnapshot.child("items")

                    val items = itemsSnapshot.children.mapNotNull { itemSnapshot ->
                        val menuItem = itemSnapshot.toMenuItem()
                        if (menuItem?.storeId == storeId) menuItem else null
                    }

                    if (items.isNotEmpty() && categoryName != null) {
                        categoryList.add(Pair(categoryId, categoryName))
                    }
                    allItemsList.addAll(items)
                }
                _categories.value = categoryList
                _menuItems.value = allItemsList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("CategoryViewModel", "Failed to load categories: ${databaseError.message}")
            }
        })
    }
    fun getAllItems() {
        _menuItems.value = allItemsList // Đặt toàn bộ item vào LiveData để hiển thị tất cả
    }

    fun filterItemsByCategory(categoryId: String) {
        val filteredItems = allItemsList.filter { it.categoryId == categoryId }
        _menuItems.value = filteredItems
    }

    private fun DataSnapshot.toMenuItem(): MenuItem? {
        val itemName = child("item_name").getValue(String::class.java)
        val itemDetail = child("item_detail").getValue(String::class.java)
        val itemImage = child("item_image").getValue(String::class.java)
        val itemPrice = child("item_price").getValue(Int::class.java)
        val storeId = child("store_id").getValue(String::class.java)
        val categoryId = this.ref.parent?.parent?.key

        return if (itemName != null && itemImage != null && itemPrice != null && storeId != null) {
            MenuItem(
                categoryId = categoryId,
                itemName = itemName,
                itemDetail = itemDetail ?: "",
                itemImage = itemImage,
                itemPrice = itemPrice,
                storeId = storeId
            )
        } else {
            null
        }
    }
}