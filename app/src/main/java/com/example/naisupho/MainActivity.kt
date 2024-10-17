package com.example.naisupho

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.example.naisupho.databinding.ActivityMainBinding
import com.example.naisupho.model.CartItems
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Locale
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var totalCartItems = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var NavController = findNavController(R.id.fragmentContainerView)
        var bottomnav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomnav.setupWithNavController(NavController)
//        binding.notificationButton.setOnClickListener {
//            val bottomSheetDialog = Notifaction_Bottom_Fragment()
//            bottomSheetDialog.show(supportFragmentManager,"Test")
//        }

        // Initialize Google Sign In (if needed)
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000 // Update location every 5 seconds
            fastestInterval = 2000 // Minimum time between location updates (optional)
        }
        updateCartItemCount()
    }
    private fun updateCartItemCount() {
        // Get the reference to the Firebase database
        val database = FirebaseDatabase.getInstance().reference
        val userId = mAuth.currentUser?.uid ?: ""

        // Get the reference to the user's cart items
        val cartItemsRef = database.child("Users").child(userId).child("CartItems")

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