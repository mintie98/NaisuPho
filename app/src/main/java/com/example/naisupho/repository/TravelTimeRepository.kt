package com.example.naisupho.repository

import android.util.Log
import com.example.naisupho.interfaces.RetrofitClient
import com.example.naisupho.interfaces.TravelTimeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class TravelTimeRepository @Inject constructor(
    @Named("GoogleClient")private val retrofitClient: RetrofitClient,
    private val apiKey: String // API Key được tiêm từ Hilt
) {
    fun getTravelTime(userLocation: String, storeLocation: String, callback: (String?) -> Unit) {
        retrofitClient.getEstimatedTravelTime(userLocation, storeLocation, apiKey).enqueue(object :
            Callback<TravelTimeResponse> {
            override fun onResponse(call: Call<TravelTimeResponse>, response: Response<TravelTimeResponse>) {
                if (response.isSuccessful) {
                    val estimatedTravelTime = response.body()?.rows?.get(0)?.elements?.get(0)?.duration?.text
                    Log.d("TravelTimeRepository", "Estimated travel time: $estimatedTravelTime")
                    callback(estimatedTravelTime)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<TravelTimeResponse>, t: Throwable) {
                Log.e("TravelTimeRepository", "Network failure: ${t.message}")
                callback(null)
            }
        })
    }
}