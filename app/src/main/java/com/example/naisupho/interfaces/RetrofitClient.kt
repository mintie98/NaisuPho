package com.example.naisupho.interfaces

import com.example.naisupho.interfaces.TravelTimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitClient {

    @GET("distancematrix/json")
    fun getEstimatedTravelTime(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("key") apiKey: String
    ): Call<TravelTimeResponse>
}