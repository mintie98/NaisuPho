package com.example.naisupho

import android.content.Context
import com.example.naisupho.utils.LocaleHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BaseBottomSheetFragment : BottomSheetDialogFragment() {
    override fun onAttach(context: Context) {
        val language = LocaleHelper.getLanguage(context)
        val newContext = LocaleHelper.wrap(context, language)
        super.onAttach(newContext)
    }
}