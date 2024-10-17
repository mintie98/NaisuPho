package com.example.naisupho.interfaces

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("maps/api/distancematrix/json")
    fun getEstimatedTravelTime(
        @Query("origins") origin: String,
        @Query("destinations") destination: String,
        @Query("key") apiKey: String,
//        @Query("mode") mode: String
    ): Call<TravelTimeResponse>
}