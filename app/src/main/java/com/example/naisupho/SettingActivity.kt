package com.example.naisupho

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.naisupho.databinding.ActivitySettingBinding
import com.example.naisupho.utils.LocaleHelper
import com.google.firebase.auth.FirebaseAuth

class SettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo ViewBinding
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cập nhật ngôn ngữ hiển thị hiện tại
        updateSelectedLanguage(LocaleHelper.getLanguage(this))

        // Xử lý nút quay lại
        binding.backArrow.setOnClickListener {
            onBackPressed()
        }

        // Xử lý khi người dùng chọn ngôn ngữ
        binding.nextArrow.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // Xử lý nút đăng xuất
        binding.logoutButton.setOnClickListener {
            val msg = getString(R.string.logout_message)
            showCustomDialog(msg)
        }
    }

    // Hiển thị hộp thoại chọn ngôn ngữ
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("Tiếng Việt", "English", "日本語")
        val languageCodes = arrayOf("vi", "en", "ja")

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.choose_language)) // Tiêu đề từ strings.xml
            .setItems(languages) { _, which ->
                setAppLocale(languageCodes[which])
            }
            .create()
            .show()
    }

    // Cập nhật ngôn ngữ và reset ứng dụng
    private fun setAppLocale(language: String) {
        // Lưu ngôn ngữ vào SharedPreferences
        LocaleHelper.saveLanguage(this, language)

        // Khởi động lại ứng dụng từ SplashScreen hoặc MainActivity
        restartApp()
    }

    private fun restartApp() {
        val intent = Intent(this, SplashScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }

    // Cập nhật ngôn ngữ hiện tại trong giao diện
    private fun updateSelectedLanguage(languageCode: String) {
        val languageName = when (languageCode) {
            "vi" -> "Tiếng Việt"
            "en" -> "English"
            "ja" -> "日本語"
            else -> "English"
        }
        binding.selectedLanguage.text = languageName
    }

    // Hiển thị hộp thoại đăng xuất
    private fun showCustomDialog(msg: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_custom_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val dialogMessage: TextView = dialog.findViewById(R.id.tvMessage)
        dialogMessage.text = msg

        val confirmButton: Button = dialog.findViewById(R.id.btnYes)
        confirmButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            dialog.dismiss()
        }

        val cancelButton: Button = dialog.findViewById(R.id.btnCancel)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}