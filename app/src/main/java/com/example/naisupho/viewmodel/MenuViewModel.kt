package com.example.naisupho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naisupho.model.MenuItem
import com.example.naisupho.model.Stores
import com.example.naisupho.repository.TravelTimeRepository
import com.example.naisupho.utils.StringUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val travelTimeRepository: TravelTimeRepository
) : ViewModel() {

    private val _menuItems = MutableLiveData<List<MenuItem>>()
    val menuItems: LiveData<List<MenuItem>> get() = _menuItems

    private val _filteredMenuItems = MutableLiveData<List<MenuItem>>()
    val filteredMenuItems: LiveData<List<MenuItem>> get() = _filteredMenuItems


    private val _stores = MutableLiveData<Map<String, Stores>>()
    val stores: LiveData<Map<String, Stores>> get() = _stores

    fun fetchTravelTime(userLocation: String, storeLocation: String, callback: (String?) -> Unit) {
        travelTimeRepository.getTravelTime(userLocation, storeLocation, callback)
    }

    fun fetchMenuItems() {
        val menuRef = FirebaseDatabase.getInstance().reference.child("menuItem")

        menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val menuItemsList = mutableListOf<MenuItem>()
                for (categorySnapshot in dataSnapshot.children) {
                    //val categoryId = categorySnapshot.key
                    //Log.d("MenuViewModel", "DataSnapshot: $categoryId")
                    val itemsSnapshot = categorySnapshot.child("items")
                    for (itemSnapshot in itemsSnapshot.children) {
                        val menuItem = itemSnapshot.toMenuItem()
                        menuItem?.let { menuItemsList.add(it) }
                    }
                }
                _menuItems.value = menuItemsList
                Log.d("MenuViewModel", "Loaded menu items: $menuItemsList")
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }

    fun fetchStores() {
        val storeRef = FirebaseDatabase.getInstance().reference.child("Stores")
        storeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(storeSnapshot: DataSnapshot) {
                val storesMap = mutableMapOf<String, Stores>()
                for (store in storeSnapshot.children) {
                    val storeInfo = store.toStore()
                    val storeId = store.key
                    if (storeId != null && storeInfo != null) {
                        storesMap[storeId] = storeInfo
                    }
                }
                _stores.value = storesMap
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }


    fun filterMenuItemsByCategory(categoryId: String) {
        Log.d("MenuViewModel", "Current menu items: ${_menuItems.value}")
        val filteredItems = _menuItems.value?.filter {
            Log.d("MenuViewModel", "Checking item: ${it.itemName}, categoryId: ${it.categoryId}")
            it.categoryId == categoryId
        } ?: emptyList()
        Log.d("MenuViewModel", "Filtered items: $filteredItems")
        _filteredMenuItems.value = filteredItems
    }
    fun searchMenuItems(query: String) {
        val normalizedQuery = StringUtils.removeVietnameseAccents(query).lowercase()
        val filteredItems = _menuItems.value?.filter {
            val itemName = it.itemName ?: ""
            val normalizedItemName = StringUtils.removeVietnameseAccents(itemName).lowercase()
            Log.d("MenuViewModel", "Comparing: '$normalizedItemName' with query: '$normalizedQuery'")
            normalizedItemName.contains(normalizedQuery)
        } ?: emptyList()
        _filteredMenuItems.value = filteredItems
        Log.d("MenuViewModel", "Search results for '$query': $filteredItems")
    }

    // Extension function to convert DataSnapshot to MenuItem
    private fun DataSnapshot.toMenuItem(): MenuItem? {
        val itemName = child("item_name").getValue(String::class.java)
        val itemDetail = child("item_detail").getValue(String::class.java)
        val itemImage = child("item_image").getValue(String::class.java)
        val itemPrice = child("item_price").getValue(Int::class.java)
        val storeId = child("store_id").getValue(String::class.java)
        val categoryId = this.ref.parent?.parent?.key
        Log.d("MenuViewModel", "DataSnapshot: $categoryId")

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

    // Extension function to convert DataSnapshot to Stores
    private fun DataSnapshot.toStore(): Stores? {
        val storeName = child("store_name").getValue(String::class.java)
        val storeAddress = child("store_address").getValue(String::class.java)
        val storePhotoUrl = child("store_photoUrl").getValue(String::class.java)
        val storeRate = child("store_rate").getValue(Float::class.java)
        val storePostcode = child("store_postcode").getValue(Int::class.java)

        return if (storeName != null && storeAddress != null && storePhotoUrl != null && storeRate != null && storePostcode != null) {
            Stores(
                storeName = storeName,
                storeAddress = storeAddress,
                storePhotoUrl = storePhotoUrl,
                storeRate = storeRate,
                storePostcode = storePostcode
            )
        } else {
            null
        }
    }
}