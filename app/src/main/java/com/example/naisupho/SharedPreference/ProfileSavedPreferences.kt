package com.example.wavesoffood.SharedPreference

import android.content.Context

class ProfileSavedPreferences {


    companion object {
        private const val PREF_FILE_NAME = "MyAppPreferences"

        private const val KEY_NAME = "name"
        private const val KEY_ADDRESS = "address"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_ITEMPUSH = "itempushkey"

        fun setName(context: Context, name: String) {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            sharedPref.edit().putString(KEY_NAME, name).apply()
        }

        fun getName(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString(KEY_NAME, null)
        }
        fun setAddress(context: Context, address: String) {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            sharedPref.edit().putString(KEY_ADDRESS, address).apply()
        }
        fun getAddress(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString(KEY_ADDRESS, null)
        }
        fun setEmail(context: Context, email: String) {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            sharedPref.edit().putString(KEY_EMAIL, email).apply()
        }

        fun getEmail(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString(KEY_EMAIL, null)
        }
        fun setPhone(context: Context, phone: String) {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            sharedPref.edit().putString(KEY_PHONE, phone).apply()
        }
        fun getPhone(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString(KEY_PHONE, null)
        }

        // saving itempushkey for shared preferences
        fun setItemPushKey(context: Context, itempushkey: String) {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            sharedPref.edit().putString(KEY_ITEMPUSH, itempushkey).apply()
        }
        fun getItemPushKey(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            return sharedPref.getString(KEY_ITEMPUSH, null)
        }
    }
}