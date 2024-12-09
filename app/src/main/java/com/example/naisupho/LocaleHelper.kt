package com.example.naisupho

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    // Áp dụng Locale cho Context
    fun wrap(context: Context, language: String): ContextWrapper {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return ContextWrapper(context.createConfigurationContext(config))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return ContextWrapper(context)
        }
    }

    // Lấy ngôn ngữ đã lưu
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return prefs.getString("My_Lang", "en") ?: "en"
    }

    // Lưu ngôn ngữ mới
    fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        prefs.putString("My_Lang", language)
        prefs.apply()
    }
}