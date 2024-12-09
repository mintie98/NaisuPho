package com.example.naisupho

import android.content.Context
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {
    override fun onAttach(context: Context) {
        val language = LocaleHelper.getLanguage(context)
        val newContext = LocaleHelper.wrap(context, language)
        super.onAttach(newContext)
    }
}