package com.example.naisupho.di

import android.app.Application
import android.content.Context
import com.example.naisupho.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    var userLocation: String? = null
    override fun attachBaseContext(base: Context) {
        // Thiết lập Locale khi ứng dụng khởi động
        val language = LocaleHelper.getLanguage(base)
        val context = LocaleHelper.wrap(base, language)
        super.attachBaseContext(context)
    }

    override fun onCreate() {
        super.onCreate()
        // Đảm bảo Locale được áp dụng đúng
        LocaleHelper.wrap(applicationContext, LocaleHelper.getLanguage(applicationContext))
    }
}