package com.example.naisupho

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.naisupho.databinding.ActivitySettingBinding
import com.google.firebase.auth.FirebaseAuth

class SettingActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrow.setOnClickListener {
            onBackPressed()
        }
        binding.logoutButton.setOnClickListener {
            val msg = "Are you sure you want to logout?"
            showCustomDialog(msg)
        }

    }

    private fun showCustomDialog(msg: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_custom_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Assuming "dialogMessage" is the id of the TextView in your custom dialog layout
        val dialogMessage: TextView = dialog.findViewById(R.id.tvMessage)
        dialogMessage.text = msg

        // Assuming "confirmButton" is the id of the confirm button in your custom dialog layout
        val confirmButton: Button = dialog.findViewById(R.id.btnYes)
        confirmButton.setOnClickListener {
            // Handle confirm action here
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        // Assuming "cancelButton" is the id of the cancel button in your custom dialog layout
        val cancelButton: Button = dialog.findViewById(R.id.btnCancel)
        cancelButton.setOnClickListener {
            // Handle cancel action here
            dialog.dismiss()
        }

        dialog.show()
    }
}