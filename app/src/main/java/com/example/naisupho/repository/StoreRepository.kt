package com.example.naisupho.repository

import android.util.Log
import com.example.naisupho.model.Stores
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class StoreRepository @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase
) {
    fun fetchStores(callback: (List<Stores>) -> Unit) {
        val storeRef = firebaseDatabase.reference.child("Stores")
        val storesList = mutableListOf<Stores>()

        storeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (storeSnapshot in dataSnapshot.children) {
                    val storeName = storeSnapshot.child("store_name").getValue(String::class.java)
                    val storeAddress = storeSnapshot.child("store_address").getValue(String::class.java)
                    val storePhotoUrl = storeSnapshot.child("store_photoUrl").getValue(String::class.java)
                    val storeRate = storeSnapshot.child("store_rate").getValue(Float::class.java)
                    val storePostcode = storeSnapshot.child("store_postcode").getValue(Int::class.java)
                    if (storeName != null && storeAddress != null && storePhotoUrl != null && storeRate != null && storePostcode != null) {
                        val store = Stores(
                            storeId = storeSnapshot.key,
                            storeName = storeName,
                            storeAddress = storeAddress,
                            storePhotoUrl = storePhotoUrl,
                            storeRate = storeRate,
                            storePostcode = storePostcode
                        )
                        storesList.add(store)
                    }
                }
                callback(storesList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi nếu cần
            }
        })
    }
    fun fetchStoreById(storeId: String, callback: (Stores) -> Unit) {
        val storeRef = firebaseDatabase.reference.child("Stores").child(storeId)
        storeRef.get().addOnSuccessListener { snapshot ->
            val store = snapshot.getValue(Stores::class.java)
            if (store != null) {
                callback(store)
            }
        }
    }
}