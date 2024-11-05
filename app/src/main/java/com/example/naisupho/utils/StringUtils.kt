// StringUtils.kt
package com.example.naisupho.utils

object StringUtils {
    fun removeVietnameseAccents(str: String): String {
        val regex = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        val temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
        return regex.replace(temp, "").replace('đ', 'd').replace('Đ', 'D')
    }
}