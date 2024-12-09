package com.example.naisupho.navFragment

import android.os.Bundle
import android.util.Log
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import kotlinx.coroutines.*
import android.net.Uri
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.naisupho.BaseFragment
import com.example.naisupho.bottomsheet.MenuBootomSheetFragment
import com.example.naisupho.NotificationActivity
import com.example.naisupho.R
import com.example.naisupho.SearchActivity
import com.example.naisupho.adapter.NearMeAdapter
import com.example.naisupho.databinding.FragmentHomeBinding
import com.example.naisupho.di.MyApp
import com.example.naisupho.model.Stores
import com.example.naisupho.viewmodel.HomeViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var userLocation: String? = null
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageSlider()
        observeStores()
        setupLocation()
        checkLocationPermissionAndRequestUpdates()
    }

    private fun setupUI() {
        binding.viewAllMenu.setOnClickListener {
            userLocation?.let {
                val menuBottomSheet = MenuBootomSheetFragment.newInstance(it) // Truyền userLocation qua newInstance
                menuBottomSheet.show(parentFragmentManager, "MenuBottomSheet")
            } ?: run {
                Log.e("HomeFragment", "User location is null")
            }
        }

        binding.notificationButton.setOnClickListener {
            startActivity(Intent(activity, NotificationActivity::class.java))
        }

        binding.searchView.setOnClickListener {
            val intent = Intent(activity, SearchActivity::class.java)
            // Truyền userLocation vào Intent
            userLocation?.let { location ->
                intent.putExtra("USER_LOCATION", location)
            }
            startActivity(intent)
        }

        binding.location.setOnClickListener {
            checkLocationPermissionAndRequestUpdates()
        }
    }

    private fun setupLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000 // Cập nhật vị trí mỗi 5 giây
            fastestInterval = 2000 // Thời gian cập nhật tối thiểu giữa các lần cập nhật
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
        } else {
            CoroutineScope(Dispatchers.Main).launch { requestLocationUpdates() }
        }
    }

    private suspend fun requestLocationUpdates() {
        val locationSettingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(true).build()
        getUserLocation()
        try {
            val result = LocationServices.getSettingsClient(requireContext()).checkLocationSettings(locationSettingsRequest).await()
            getUserLocation()
        } catch (e: ApiException) {
            when (e.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    (e as ResolvableApiException).startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                }
            }
        }
    }

    private suspend fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location = withContext(Dispatchers.IO) { fusedLocationClient.lastLocation.await() }
            location?.let {
                val locationString = "${it.latitude}, ${it.longitude}"
                Log.d("HomeFragment", "User location: $locationString")
                viewModel.updateUserLocation(locationString) // Cập nhật vào ViewModel
                userLocation = locationString
                (requireActivity().application as MyApp).userLocation = locationString
                updateUIWithLocation(it.latitude, it.longitude)
            }
        }
    }

    private suspend fun updateUIWithLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = withContext(Dispatchers.IO) { geocoder.getFromLocation(latitude, longitude, 1) }

        addresses?.firstOrNull()?.let { address ->
            val addressLine = address.getAddressLine(0)
            binding.txtLocation.text = addressLine
            binding.txtLocation.setOnClickListener {
                openLocationInMaps(addressLine)
            }
        } ?: Log.e("HomeFragment", "Không tìm thấy địa chỉ")
    }

    private fun openLocationInMaps(location: String) {
        val uri = Uri.parse("geo:0,0?q=$location")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
        startActivity(intent)
    }

    private fun setupImageSlider() {
        val imageList = listOf(
            SlideModel(R.drawable.banner1, ScaleTypes.FIT),
            SlideModel(R.drawable.banner2, ScaleTypes.FIT),
            SlideModel(R.drawable.banner3, ScaleTypes.FIT)
        )
        binding.imageSlider.setImageList(imageList, ScaleTypes.FIT)
    }

    private fun observeStores() {
        viewModel.stores.observe(viewLifecycleOwner) { storesList ->
            setAdapter(storesList)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.travelTimes.observe(viewLifecycleOwner) { travelTimes ->
            // Khi thời gian di chuyển được tính toán xong, cập nhật UI tại đây
            (binding.itemRecycleView.adapter as? NearMeAdapter)?.updateTravelTimes(travelTimes)
        }

        viewModel.userLocation.observe(viewLifecycleOwner) { location ->
            Log.d("HomeFragment", "Fetching stores for user location: $location")
            viewModel.fetchStores(location)
        }
    }

    private fun setAdapter(storesList: List<Stores>) {
        val adapter = NearMeAdapter(storesList, requireContext() )
        binding.itemRecycleView.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.itemRecycleView.adapter = adapter

        // Khi đã có travelTimes từ viewModel
        viewModel.travelTimes.observe(viewLifecycleOwner) { travelTimes ->
            adapter.updateTravelTimes(travelTimes)
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_CHECK_SETTINGS = 200

    }
}


