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
class MainActivity : AppCompatActivity() {

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
        // Get the current user's ID
        val userId = mAuth.currentUser?.uid ?: return

        // Reference to the user's cart items in the separate "CartItems" node
        val cartItemsRef = FirebaseDatabase.getInstance().reference.child("CartItems").child(userId)

        // Listen for changes in the cart items
        cartItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Reset the total cart items count
                totalCartItems = 0

                // Loop through all the cart items and add their quantities to the total
                for (cartItemSnapshot in dataSnapshot.children) {
                    val cartItem = cartItemSnapshot.getValue(CartItems::class.java)
                    totalCartItems += cartItem?.itemQuantity ?: 0
                }

                // Update the badge
                val menuItemId = R.id.cartFragment
                if (totalCartItems == 0) {
                    // If there are no items in the cart, hide the badge
                    binding.bottomNavigationView.removeBadge(menuItemId)
                } else {
                    // If there are items in the cart, show the badge and set its number
                    val badgeDrawable = binding.bottomNavigationView.getOrCreateBadge(menuItemId)
                    badgeDrawable.number = totalCartItems
                    badgeDrawable.isVisible = true
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.w(TAG, "Failed to read cart items.", databaseError.toException())
            }
        })
    }
}