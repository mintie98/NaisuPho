package com.example.naisupho

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var searchEditText: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        Log.d("SearchActivity", "SearchActivity started")

        backButton = findViewById(R.id.back_button)
        searchEditText = findViewById(R.id.searchView)

        backButton.setOnClickListener {
            finish()
        }
    }
}