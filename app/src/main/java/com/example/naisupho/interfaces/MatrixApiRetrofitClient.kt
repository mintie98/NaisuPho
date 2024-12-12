package com.example.naisupho.interfaces

import com.example.naisupho.model.TravelTimeResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MatrixApiRetrofitClient {

    @GET("distancematrix/json")
    fun getEstimatedTravelTime(
        @Query("origins") origins: String,
        @Query("destinations") destinations: String,
        @Query("key") apiKey: String
    ): Call<TravelTimeResponse>
}