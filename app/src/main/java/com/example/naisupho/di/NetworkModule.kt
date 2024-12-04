package com.example.naisupho.di

import com.example.naisupho.BuildConfig
import com.example.naisupho.interfaces.RetrofitClient
import com.example.naisupho.interfaces.ZipcloudApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Cấu hình OkHttpClient dùng chung
    @Provides
    @Singleton
    @Named("CommonClient")
    fun provideCommonOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }


    // Cấu hình OkHttpClient cho Zipcloud với API key
    @Provides
    @Singleton
    @Named("ZipcloudClient")
    fun provideZipcloudOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    // Cấu hình Retrofit cho Google API
    //Google Matrix API
    @Provides
    @Singleton
    @Named("GoogleRetrofit")
    fun provideGoogleRetrofit(@Named("CommonClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Cấu hình Retrofit cho Zipcloud API
    @Provides
    @Singleton
    @Named("ZipcloudRetrofit")
    fun provideZipcloudRetrofit(@Named("ZipcloudClient") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://zipcloud.ibsnet.co.jp/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Tạo RetrofitClient cho Google API
    //anotation
    @Provides
    @Singleton
    @Named("GoogleClient")
    fun provideGoogleRetrofitClient(@Named("GoogleRetrofit") retrofit: Retrofit): RetrofitClient {
        return retrofit.create(RetrofitClient::class.java)
    }

    // Tạo ZipcloudApiService để gọi API Zipcloud
    @Provides
    @Singleton
    @Named("ZipcloudClient")
    fun provideZipcloudApiService(@Named("ZipcloudRetrofit") retrofit: Retrofit): ZipcloudApiService {
        return retrofit.create(ZipcloudApiService::class.java)
    }

    // Cung cấp API key cho Google API
    @Provides
    @Named("GoogleApiKey")
    fun provideGoogleApiKey(): String = BuildConfig.GOOGLE_Martrix_API

}