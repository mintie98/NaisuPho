package com.example.naisupho

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.naisupho.databinding.ActivityMainBinding
import com.example.naisupho.model.CartItems
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private var totalCartItems = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.fragmentContainerView)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setupWithNavController(navController)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Update cart item count badge
        updateCartItemCount()
    }

    private fun updateCartItemCount() {
        val userId = mAuth.currentUser?.uid ?: return

        val cartItemsRef = FirebaseDatabase.getInstance().reference.child("CartItems").child(userId)

        cartItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Đếm số lượng nhóm hàng StoreCartItem trong giỏ hàng
                val storeCartItemCount = dataSnapshot.childrenCount.toInt()

                // Cập nhật badge trên bottom navigation view
                val menuItemId = R.id.cartFragment
                if (storeCartItemCount == 0) {
                    binding.bottomNavigationView.removeBadge(menuItemId)
                } else {
                    val badgeDrawable = binding.bottomNavigationView.getOrCreateBadge(menuItemId)
                    badgeDrawable.number = storeCartItemCount
                    badgeDrawable.isVisible = true
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "Failed to read cart items.", databaseError.toException())
            }
        })
    }
}