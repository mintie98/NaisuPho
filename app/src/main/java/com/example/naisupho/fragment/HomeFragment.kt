package com.example.naisupho.fragment

import android.os.Bundle
import android.util.Log
import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.*
import android.net.Uri
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.naisupho.MenuBootomSheetFragment
import com.example.naisupho.R
import com.example.naisupho.adapter.NearMeAdapter
import com.example.naisupho.databinding.FragmentHomeBinding
import com.example.naisupho.model.MenuItem
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var itemName: MutableList<String>
    private lateinit var itemPrice: MutableList<String>
    private lateinit var itemImage: MutableList<String>
    private lateinit var storeName: MutableList<String>
    private lateinit var rate: MutableList<String>
    private lateinit var distance: MutableList<String>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewAllMenu.setOnClickListener {
            val bottomSheetDialog = MenuBootomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "Test")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageSlider()
        fetchMenuItems()
        setupLocation()
        binding.location.setOnClickListener {
            checkLocationPermissionAndRequestUpdates()
        }
    }
    fun setupLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000 // Update location every 5 seconds
            fastestInterval = 2000 // Minimum time between location updates (optional)
        }
    }

    private fun checkLocationPermissionAndRequestUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
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

    private fun setupImageSlider() {
        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.banner1, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner2, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner3, ScaleTypes.FIT))
        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList)
        imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }

        private suspend fun requestLocationUpdates() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(requireContext()).checkLocationSettings(builder.build())
        try {
            val response = result.await()
            getUserLocation()
        } catch (e: ApiException) {
            when (e.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    val resolvableApiException = e as ResolvableApiException
                    resolvableApiException.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
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
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        val location = withContext(Dispatchers.IO) {
            fusedLocationClient.lastLocation.await()
        }
        if (location != null) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
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

    @Deprecated("Deprecated in Java")
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

    private fun fetchMenuItems() {
        binding.progressBar.visibility = View.VISIBLE
        database = FirebaseDatabase.getInstance()
        val itemRef: DatabaseReference = database.reference.child("menuItems")

        itemName = mutableListOf()
        itemPrice = mutableListOf()
        storeName = mutableListOf()
        itemImage = mutableListOf()
        rate = mutableListOf()
        distance = mutableListOf()
        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (foodSnapshot in dataSnapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.itemName?.let {
                        itemName.add(it)
                        Log.d("FirebaseData", "MenuItem: $it")
                    }
                    menuItem?.itemPrice?.let {
                        itemPrice.add(it.toString())
                    }
                    menuItem?.storeName?.let {
                        storeName.add(it)
                    }
                    menuItem?.itemImage?.let {
                        itemImage.add(it)
                    }
                    menuItem?.rate?.let {
                        rate.add(it.toString())
                    }
                    menuItem?.distance?.let {
                        distance.add(it.toString())
                    }
                }

                settingMenuItemInRandomOrder()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Error: ${databaseError.message}")
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun settingMenuItemInRandomOrder() {
        val indices = distance.indices.sortedBy { distance[it].toDoubleOrNull() ?: Double.MAX_VALUE }
        val shuffledFoodNames = indices.map { itemName[it] }
        val shuffledFoodPrices = indices.map { itemPrice[it] }
        val shuffledImageUris = indices.map { itemImage[it] }
        val shuffledStoreName = indices.map { storeName[it] }
        val shuffledRate = indices.map { rate[it] }
        val shuffledDistance = indices.map { distance[it] }

        val numItemsToShow = 6
        val subsetItemNames = shuffledFoodNames.take(numItemsToShow)
        val subsetItemPrices = shuffledFoodPrices.take(numItemsToShow)
        val subsetImageUris = shuffledImageUris.take(numItemsToShow)
        val subsetStoreName = shuffledStoreName.take(numItemsToShow)
        val subsetRate = shuffledRate.take(numItemsToShow)
        val subsetDistance = shuffledDistance.take(numItemsToShow)

        setAdapter(subsetItemNames, subsetItemPrices, subsetImageUris, subsetStoreName, subsetRate, subsetDistance)
    }

    private fun setAdapter(
        subsetItemNames: List<String>,
        subsetItemPrices: List<String>,
        subsetImageUris: List<String>,
        subsetStoreName: List<String>,
        subsetRate: List<String>,
        subsetDistance: List<String>
    ) {
        val adapter = NearMeAdapter(
            subsetItemNames, subsetItemPrices, subsetImageUris, subsetStoreName, subsetRate, subsetDistance,
            requireContext()
        )
        binding.itemRecycleView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.itemRecycleView.adapter = adapter
    }
}
