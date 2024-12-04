package com.example.naisupho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.naisupho.model.CartItems
import com.example.naisupho.model.StoreCartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _storeCartItems = MutableLiveData<List<StoreCartItem>>()
    val storeCartItems: LiveData<List<StoreCartItem>> get() = _storeCartItems

    private val _cartItems = MutableLiveData<List<CartItems>>()
    val cartItems: LiveData<List<CartItems>> get() = _cartItems
    private val _itemKeys = mutableListOf<String>()  // Lưu danh sách các itemKey

    private val _totalCost = MutableLiveData<Int>()
    val totalCost: LiveData<Int> get() = _totalCost


    private val _defaultAddress = MutableLiveData<String>()
    val defaultAddress: LiveData<String> get() = _defaultAddress

    private val _isCartEmpty = MutableLiveData<Boolean>()
    val isCartEmpty: LiveData<Boolean> get() = _isCartEmpty

    init {
        fetchCartItems()
        observeCartExistence() // Kiểm tra xem giỏ hàng có trống không
    }
    fun fetchStoreName(storeId: String, callback: (String) -> Unit) {
        val storesRef = firebaseDatabase.getReference("Stores").child(storeId)
        storesRef.child("store_name").get().addOnSuccessListener { snapshot ->
            val storeName = snapshot.value as? String ?: "Unknown Store"
            callback(storeName)
        }.addOnFailureListener {
            Log.e("CartViewModel", "Failed to fetch store name for storeId: $storeId")
            callback("Unknown Store")
        }
    }

    private fun fetchCartItems() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val cartRef = firebaseDatabase.getReference("CartItems/$userId")
        val storesRef = firebaseDatabase.getReference("Stores")

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storeCartItems = mutableListOf<StoreCartItem>()

                if (snapshot.exists().not()) {
                    // Nếu không có dữ liệu trong snapshot, cập nhật LiveData với danh sách trống
                    _storeCartItems.value = emptyList()
                    return
                }

                snapshot.children.forEach { storeSnapshot ->
                    val storeId = storeSnapshot.key ?: return@forEach
                    var itemQuantity = 0
                    var totalPrice = 0
                    val itemsList = storeSnapshot.children.mapNotNull { itemSnapshot ->
                        val item = itemSnapshot.getValue(CartItems::class.java) ?: return@mapNotNull null
                        itemQuantity += item.itemQuantity ?: 0
                        totalPrice += (item.itemPrice ?: 0) * (item.itemQuantity ?: 0)
                        item
                    }

                    storesRef.child(storeId).get().addOnSuccessListener { storeSnapshot ->
                        val storeName = storeSnapshot.child("store_name").value as? String ?: "Unknown Store"
                        val storeImage = storeSnapshot.child("store_photoUrl").value as? String ?: ""
                        val storeCartItem = StoreCartItem(
                            storeId = storeId,
                            storeName = storeName,
                            storeImage = storeImage,
                            itemQuantity = itemQuantity,
                            totalPrice = totalPrice,
                            itemList = itemsList
                        )
                        storeCartItems.add(storeCartItem)
                        // Cập nhật LiveData với danh sách đầy đủ hoặc trống
                        _storeCartItems.value = storeCartItems
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần thiết
            }
        })
    }
    fun deleteCart(storeId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val cartRef = firebaseDatabase.getReference("CartItems/$userId/$storeId")

        cartRef.removeValue().addOnSuccessListener {
            // Nếu xóa thành công, bạn có thể cập nhật lại danh sách giỏ hàng
            fetchCartItems() // Cập nhật lại LiveData
        }.addOnFailureListener { error ->
            Log.e("CartViewModel", "Failed to delete cart: ${error.message}")
        }
    }

    fun fetchCartItemsForStore(storeId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val cartRef = firebaseDatabase.getReference("CartItems/$userId/$storeId")

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<CartItems>()
                val itemKeys = mutableListOf<String>()
                var calculatedTotalPrice = 0 // Sử dụng biến cục bộ

                snapshot.children.forEach { itemSnapshot ->
                    val cartItem = itemSnapshot.getValue(CartItems::class.java)
                    cartItem?.let {
                        itemKeys.add(itemSnapshot.key ?: "")
                        cartItems.add(it)
                        calculatedTotalPrice += (it.itemPrice ?: 0) * (it.itemQuantity ?: 1)
                    }
                }
                _cartItems.value = cartItems
                _itemKeys.clear()
                _itemKeys.addAll(itemKeys)
                _totalCost.value = calculatedTotalPrice // Cập nhật tổng giá
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })
    }

    // Hàm cập nhật tổng chi phí khi có thay đổi số lượng từ Adapter
    fun updateTotalCost(difference: Int) {
        _totalCost.value = (_totalCost.value ?: 0) + difference
    }
    fun getItemKeyAt(position: Int): String {
        return _itemKeys.getOrNull(position) ?: ""
    }


    fun updateItemQuantity(storeId: String, itemKey: String, newQuantity: Int) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val itemRef = firebaseDatabase.getReference("CartItems/$userId/$storeId/$itemKey/itemQuantity")

        itemRef.setValue(newQuantity).addOnSuccessListener {
        }
    }

    fun deleteCartItem(storeId: String, position: Int) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val itemKey = getItemKeyAt(position)

        // Kiểm tra nếu `itemKey` hợp lệ trước khi tiếp tục
        if (itemKey.isNotEmpty() && position < _itemKeys.size) {
            val itemRef = firebaseDatabase.getReference("CartItems/$userId/$storeId/$itemKey")

            itemRef.removeValue().addOnSuccessListener {
                // Chỉ xóa khỏi danh sách `_itemKeys` nếu vị trí hợp lệ
                if (position < _itemKeys.size) {
                    _itemKeys.removeAt(position)
                    fetchCartItemsForStore(storeId) // Tải lại các item sau khi xóa
                }
            }.addOnFailureListener {
                Log.e("CartViewModel", "Failed to delete item at position: $position")
            }
        } else {
            Log.e("CartViewModel", "Invalid position or itemKey: Position $position, ItemKey $itemKey")
        }
    }

    fun fetchDefaultAddress() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val userRef = firebaseDatabase.getReference("Users/$userId/addresses")

        userRef.get().addOnSuccessListener { snapshot ->
            var addressFound = false
            snapshot.children.forEach { addressSnapshot ->
                val isDefault = addressSnapshot.child("default").getValue(Boolean::class.java) ?: false
                if (isDefault) {
                    val address = addressSnapshot.child("address1").value.toString() + ", " +
                            addressSnapshot.child("address2").value.toString()
                    _defaultAddress.value = address
                    addressFound = true
                    return@forEach
                }
            }
            if (!addressFound) {
                _defaultAddress.value = ""
            }
        }.addOnFailureListener {
            _defaultAddress.value = "delivery address not found"
        }
    }
    private fun observeCartExistence() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val cartRef = firebaseDatabase.getReference("CartItems/$userId")

        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _isCartEmpty.value = !snapshot.exists() // Giỏ hàng trống nếu không có dữ liệu
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartViewModel", "Failed to observe cart existence: ${error.message}")
            }
        })
    }
}