package com.example.naisupho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naisupho.model.MenuItem
import com.example.naisupho.model.Stores
import com.example.naisupho.repository.TravelTimeRepository
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
                    val itemsSnapshot = categorySnapshot.child("items")
                    for (itemSnapshot in itemsSnapshot.children) {
                        val menuItem = itemSnapshot.toMenuItem()
                        menuItem?.let { menuItemsList.add(it) }
                    }
                }
                _menuItems.value = menuItemsList
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

    // Extension function to convert DataSnapshot to MenuItem
    private fun DataSnapshot.toMenuItem(): MenuItem? {
        val itemName = child("item_name").getValue(String::class.java)
        val itemDetail = child("item_detail").getValue(String::class.java)
        val itemImage = child("item_image").getValue(String::class.java)
        val itemPrice = child("item_price").getValue(Int::class.java)
        val storeId = child("store_id").getValue(String::class.java)

        return if (itemName != null && itemImage != null && itemPrice != null && storeId != null) {
            MenuItem(
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
        val storeRate = child("store_rate").getValue(Double::class.java)
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