package com.example.naisupho.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    var userLocation: String? = null
    override fun onCreate() {
        super.onCreate()
    }
}