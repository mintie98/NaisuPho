package com.example.naisupho

import android.Manifest
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var NavController= findNavController(R.id.fragmentContainerView)
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

        binding.location.setOnClickListener {
            checkLocationPermissionAndRequestUpdates()
        }
    }

    private fun checkLocationPermissionAndRequestUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }
        // Sử dụng coroutine để thực hiện hoạt động không đồng bộ
        CoroutineScope(Dispatchers.Main).launch {
            requestLocationUpdates()
        }
    }

    private suspend fun requestLocationUpdates() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(this@MainActivity).checkLocationSettings(builder.build())
        try {
            val response = result.await()
            getUserLocation()
        } catch (e: ApiException) {
            when (e.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    val resolvableApiException = e as ResolvableApiException
                    resolvableApiException.startResolutionForResult(this@MainActivity, REQUEST_CHECK_SETTINGS)
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    // Location settings cannot be changed
                }
            }
        }
    }
    @Suppress("DEPRECATION")
    private suspend fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        val location = withContext(Dispatchers.IO) {
            fusedLocationClient.lastLocation.await()
        }
        if (location != null) {
            val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
            try {
                val addresses: List<Address>? =
                    withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    }
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressLine = address.getAddressLine(0)
                    withContext(Dispatchers.Main) {
                        binding.txtLocation.text = addressLine
                        binding.txtLocation.setOnClickListener {
                            openLocation(addressLine)
                        }
                    }

                } else {
                    // Handle the case where no addresses are found (e.g., display an error message)
                    Log.e("TAG", "No address found")
                }

            } catch (e: IOException) {
                Log.e("TAG", "Error getting location address: $e")
            }
        }
    }


    private fun openLocation(location: String) {
        val uri = Uri.parse("geo:0, 0?q=$location")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CoroutineScope(Dispatchers.Main).launch {
                    requestLocationUpdates()
                }            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                CoroutineScope(Dispatchers.Main).launch {
                    requestLocationUpdates()
                }            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_CHECK_SETTINGS = 200
    }
}