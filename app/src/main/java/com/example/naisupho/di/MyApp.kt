package com.example.naisupho.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val DistanceMatrixApi = "AIzaSyCrTEOkhRKZp2OV1c2OCWb1vG6yukgMkdc"
    }
}