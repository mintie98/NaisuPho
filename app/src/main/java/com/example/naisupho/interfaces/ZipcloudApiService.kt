package com.example.naisupho.interfaces

import com.example.naisupho.model.ZipcloudResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ZipcloudApiService {
    @GET("search")
    fun getAddressByZipcode(
        @Query("zipcode") zipcode: String
    ): Call<ZipcloudResponse>
}