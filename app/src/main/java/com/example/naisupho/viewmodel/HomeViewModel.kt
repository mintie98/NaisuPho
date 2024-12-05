package com.example.naisupho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naisupho.model.Stores
import com.example.naisupho.repository.StoreRepository
import com.example.naisupho.repository.TravelTimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val travelTimeRepository: TravelTimeRepository,
    private val repository: StoreRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _userLocation = MutableLiveData<String>()
    val userLocation: LiveData<String> get() = _userLocation

    private val _stores = MutableLiveData<List<Stores>>()
    val stores: LiveData<List<Stores>> get() = _stores

    private val _travelTimes = MutableLiveData<Map<String, String>>()
    val travelTimes: LiveData<Map<String, String>> get() = _travelTimes

    // Cập nhật vị trí người dùng
    fun updateUserLocation(location: String) {
        _userLocation.value = location
    }

    // Lấy thời gian di chuyển đến các cửa hàng
    fun fetchTravelTime(userLocation: String, storeAddress: String, storeId: String) {
        travelTimeRepository.getTravelTime(userLocation, storeAddress) { time ->
            val updatedTravelTimes = _travelTimes.value?.toMutableMap() ?: mutableMapOf()
            updatedTravelTimes[storeId] = time ?: "N/A"
            _travelTimes.postValue(updatedTravelTimes)
        }
    }

    // Lấy danh sách cửa hàng và cập nhật trạng thái tải
    fun fetchStores(userLocation: String) = viewModelScope.launch {
        _isLoading.postValue(true) // Bắt đầu tải
        try {
            repository.fetchStores { storesList ->
                _stores.postValue(storesList)

                storesList.forEach { store ->
                    Log.d("StoreViewModel", "Store ID: ${store.storeId}, Store Address: ${store.storeAddress}")
                    store.storeId?.let {
                        fetchTravelTime(userLocation, storeAddress = store.storeAddress ?: "", storeId = it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching stores: ${e.message}")
        } finally {
            _isLoading.postValue(false) // Kết thúc tải
        }
    }
}