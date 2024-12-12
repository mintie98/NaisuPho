package com.example.naisupho

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.naisupho.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.wrap(newBase, language)
        super.attachBaseContext(context)
    }
}